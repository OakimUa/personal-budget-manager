package org.oakim.pbm.view

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.*
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.stage.PopupWindow
import org.controlsfx.control.PopOver
import org.controlsfx.control.spreadsheet.GridBase
import org.controlsfx.control.spreadsheet.SpreadsheetCellType
import org.controlsfx.control.spreadsheet.SpreadsheetView
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.parsing.*
import org.oakim.pbm.service.LocalStorage
import org.oakim.pbm.view.component.comboWithField
import org.oakim.pbm.view.component.segmentedPBMEntityMultiSelector
import org.oakim.pbm.view.component.testTransactionTable
import tornadofx.*
import tornadofx.controlsfx.popover
import tornadofx.controlsfx.toggleswitch
import java.util.concurrent.Callable

class ParserOverview : View("Parser Builder", Glyph("FontAwesome", FontAwesome.Glyph.UPLOAD)), ScopedInstance {
    private val parserModel = Parser.Model(Parser())
    private val parseTransactionDefinitionModel = ParseTransactionDefinition.Model(ParseTransactionDefinition())
    //    private val newTransactionRule = SimpleBooleanProperty(true)
    private val newTransactionRuleAccessor = SimpleObjectProperty<(() -> ParserRule)?>(null)
    private val conditions = mutableListOf<ParseCondition>().observable()

    private val rawData = SimpleStringProperty()

    private val maxColumns = 50
    private val grid = GridBase(0, maxColumns)
    private val spreadsheetView = SpreadsheetView(grid)
    private val transactions = mutableListOf<Pair<ParserRule, Transaction>>().observable()
    private var onCellPressOperation: (List<Pair<Int, String>>) -> Unit = {}
    private var conditionsTable: TableView<ParseCondition> by singleAssign()
    private var ruleListView: ListView<ParserRule> by singleAssign()

    override val root = borderpane {
        top = hbox(10) {
            addClass(Styles.content)
            button("Select file").action {
                rawData.value = chooseFile("Select file", filters = arrayOf()).firstOrNull()?.readText()
            }
            val parserComboBox = ComboBox(LocalStorage.parserNames)
            this += parserComboBox
            parserComboBox.selectionModel.selectedItemProperty().onChange {
                if (it != null)
                    parserModel.item = LocalStorage.loadParser(it)
            }
            button("New parser").action {
                parserComboBox.selectionModel.clearSelection()
                parserModel.item = Parser()
            }
            button("Evaluate") {
                enableWhen { rawData.isNotNull }
            }.action {
                transactions.clear()
                parserModel.commit()
                transactions.addAll(parserModel.item.testParse(rawData.value))
            }
        }
        left = vbox(10) {
            addClass(Styles.content)
            this += spreadsheetView
            spreadsheetView.maxWidthProperty().bind(this@borderpane.widthProperty().divide(1.5))
            spreadsheetView.minWidthProperty().bind(this@borderpane.widthProperty().divide(1.5))
            testTransactionTable(transactions, 15) {
                onUserSelect {
                    ruleListView.selectionModel.select(it.first)
                }
            }
        }
        center = form {
            fieldset {
                field("Parser name") {
                    hbox(10) {
                        textfield(parserModel.name)
                        button("Save").action {
                            parserModel.commit()
                            LocalStorage.saveParser(parserModel.item)
                        }
                    }
                }
                field("Line separator") { comboWithField(parserModel.lineSeparator, mapOf("\n" to "New line (\\n)", "\t" to "Tab  (\\t)")) }
                field("Cell separator") { comboWithField(parserModel.cellSeparator, mapOf("\n" to "New line (\\n)", "\t" to "Tab  (\\t)", "," to "Comma (,)", "." to "Dot (.)", ":" to "Colon (:)", ";" to "Semicolon (;)")) }
                field("Decimal point") { comboWithField(parserModel.decimalPoint, mapOf("," to "Comma (,)", "." to "Dot (.)")) }
                field("Thousand separator") { comboWithField(parserModel.thousandSeparator, mapOf("," to "Comma (,)", "." to "Dot (.)")) }
                field("Wallet") { combobox(parserModel.wallet, LocalStorage.wallets).cellFormat { graphic = it.displayGlyph; text = it.name } }
            }
            fieldset("Rules") {
                borderpane {
                    center = listview(parserModel.rules) {
                        ruleListView = this
                        fixedCellSize = 30.0
                        prefHeightProperty().bind(fixedCellSizeProperty().multiply(5))
                        onUserSelect { selectedRule ->
                            parseTransactionDefinitionModel.item = selectedRule.definition
                            conditions.clear()
                            conditions.addAll(selectedRule.conditions)
//                            newTransactionRule.value = false
                            newTransactionRuleAccessor.value = { selectedRule }
                        }
                        cellFormat { rule ->
                            graphic = hbox(10) {
                                this += rule.definition.type.displayGlyph
                                this += Label("[" + rule.definition.tags.joinToString(", ") { it.value } + "]")
                                this += Label("When " + rule.conditions.joinToString(", ") { it.asLabelText() })
                            }
                        }
                        contextmenu {
                            item("Duplicate").action {
                                selectedItem?.also { item ->
                                    val newItem = item.copy()
                                    parserModel.rules.value = ruleListView.items.toMutableList()
                                            .apply { this.add(this.indexOf(item), newItem) }.observable()
                                    selectionModel.select(newItem)
                                }
                            }
                        }
                    }
                    right = vbox {
                        button(graphic = Glyph("FontAwesome", FontAwesome.Glyph.ARROW_UP)) {
                            enableWhen { ruleListView.selectionModel.selectedItemProperty().isNotNull }
                        }.action {
                            parserModel.rules.value = ruleListView.items.toMutableList().apply { moveUp(ruleListView.selectedItem) }.observable()
                        }
                        button(graphic = Glyph("FontAwesome", FontAwesome.Glyph.ARROW_DOWN)) {
                            enableWhen { ruleListView.selectionModel.selectedItemProperty().isNotNull }
                        }.action {
                            parserModel.rules.value = ruleListView.items.toMutableList().apply { moveDown(ruleListView.selectedItem) }.observable()
                        }
                    }
                }
            }
            fieldset("Create transaction rule") {
                //                textProperty.bind(Bindings.createStringBinding(Callable { if (newTransactionRule.value) "Create transaction rule" else "Change transaction rule" }, newTransactionRule))
                textProperty.bind(Bindings.createStringBinding(Callable { if (newTransactionRuleAccessor.value == null) "Create transaction rule" else "Change transaction rule" }, newTransactionRuleAccessor))
                val selectorTG = ToggleGroup()
                field {
                    combobox(parseTransactionDefinitionModel.type, TransactionType.values().toList()).cellFormat { graphic = it.displayGlyph; text = it.name }
                    combobox(parseTransactionDefinitionModel.targetWallet, LocalStorage.wallets) {
                        visibleWhen {
                            Bindings.createBooleanBinding(Callable {
                                parseTransactionDefinitionModel.type.value == TransactionType.TRANSFER_OUT ||
                                        parseTransactionDefinitionModel.type.value == TransactionType.TRANSFER_IN
                            }, parseTransactionDefinitionModel.type)
                        }
                    }.cellFormat { graphic = it.displayGlyph; text = it.name }
                }
                field("Amount in cell") {
                    togglebutton("Select on table...", group = selectorTG, selectFirst = false) {
                        action {
                            if (isSelected) {
                                onCellPressOperation = { columns ->
                                    parseTransactionDefinitionModel.amountCell.value = columns.first().first
                                    val amountCondition = ParseCondition(columns.first().first,
                                            if (parseTransactionDefinitionModel.invertAmount.value) MatchingOperation.NUMERIC_NEGATIVE else MatchingOperation.NUMERIC_POSITIVE)
                                    conditions.add(0, amountCondition)
                                    isSelected = false
                                    onCellPressOperation = {}
                                }
                            } else {
                                onCellPressOperation = {}
                            }
                        }
                    }
                    label(parseTransactionDefinitionModel.amountCell)
                    toggleswitch("Inverted", parseTransactionDefinitionModel.invertAmount) {
                        selectedProperty().onChange { isInverted ->
                            conditions.firstOrNull {
                                it.cellIndex == parseTransactionDefinitionModel.amountCell.value &&
                                        (it.operation == MatchingOperation.NUMERIC_NEGATIVE || it.operation == MatchingOperation.NUMERIC_POSITIVE)
                            }?.apply {
                                operation = if (isInverted)
                                    MatchingOperation.NUMERIC_NEGATIVE
                                else
                                    MatchingOperation.NUMERIC_POSITIVE
                                conditionsTable.refresh()
                            }
                        }
                    }
                }
                field("Tags") {
                    val tags = mutableListOf<Tag>().observable()
                    parseTransactionDefinitionModel.itemProperty.onChange {
                        tags.clear()
                        tags.addAll(it?.tags ?: listOf())
                    }
                    val tagSelector: PopOver = popover(anchorLocation = PopupWindow.AnchorLocation.CONTENT_TOP_LEFT,
                            arrowLocation = PopOver.ArrowLocation.BOTTOM_RIGHT) {
                        form {
                            fieldset {
                                LocalStorage.tagAspects.forEach { aspect ->
                                    field(aspect.let { if (it.isNotBlank()) it else "</>" }) {
                                        segmentedPBMEntityMultiSelector(
                                                selected = tags,
                                                values = LocalStorage.tags,
                                                filterExtension = { it.aspect == aspect })
                                    }
                                }
                                field(forceLabelIndent = true) {
                                    button("OK").action {
                                        this@popover.hide()
                                        parseTransactionDefinitionModel.tags.value = listOf(*tags.toTypedArray()).observable()
                                    }
                                }
                            }
                        }
                    }
                    label {
                        parseTransactionDefinitionModel.tags.onChange { change ->
                            text = change?.joinToString(", ") ?: ""
                        }
                        graphic = button(graphic = Glyph("FontAwesome", FontAwesome.Glyph.ELLIPSIS_H)) {
                            action {
                                tagSelector.show(this)
                            }
                        }
                    }
                }
                field("Notes in") {
                    togglebutton("Select on table...", group = selectorTG, selectFirst = false) {
                        action {
                            if (isSelected) {
                                onCellPressOperation = { columns ->
                                    parseTransactionDefinitionModel.notesCells.value = columns.map { it.first }.distinct().observable()
                                }
                            } else {
                                onCellPressOperation = {}
                            }
                        }
                    }
                    label(parseTransactionDefinitionModel.notesCells)
                }
                field {
                    vbox {
                        tableview(conditions) {
                            conditionsTable = this
                            fixedCellSize = 30.0
                            prefHeightProperty().bind(fixedCellSizeProperty().multiply(4).add(30))
                            isEditable = true
                            enableCellEditing()
                            column("", ParseCondition::cellIndex).cellFormat {
                                graphic = ToggleButton("", Glyph("FontAwesome", FontAwesome.Glyph.HAND_ALT_UP)).apply {
                                    action {
                                        if (isSelected) {
                                            onCellPressOperation = { columns ->
                                                println(columns)
                                                rowItem.cellIndex = columns.first().first
                                                rowItem.value = columns.first().second
                                                refresh()
                                                isSelected = false
                                                onCellPressOperation = {}
                                            }
                                        } else {
                                            onCellPressOperation = {}
                                        }
                                    }
                                }
                            }
                            column("Cell", ParseCondition::cellIndex).makeEditable()
                            column("Operation", ParseCondition::operation).apply {
                                cellFactory = ComboBoxTableCell.forTableColumn<ParseCondition, MatchingOperation>(MatchingOperation.values().toList().observable())
                            }
                            column("Value", ParseCondition::value).makeEditable().remainingWidth()
                            column("", ParseCondition::cellIndex).cellFormat {
                                graphic = Button("", Glyph("FontAwesome", FontAwesome.Glyph.MINUS)).apply {
                                    action {
                                        conditions.remove(this@cellFormat.rowItem)
                                        addNewCondition()
                                    }
                                }
                            }
                            requestResize()
                        }
                    }
                }
                field(forceLabelIndent = true) {
                    buttonbar {
                        button("New rule").action {
                            conditions.clear()
                            addNewCondition()
                            parseTransactionDefinitionModel.item = ParseTransactionDefinition()
//                            newTransactionRule.value = true
                            newTransactionRuleAccessor.value = null
                            ruleListView.selectionModel.clearSelection()
                        }
                        button("Delete rule").action {
                            parserModel.rules.value = listOf(*parserModel.rules.value.filterNot { it.definition == parseTransactionDefinitionModel.item }.toTypedArray()).observable()
                            conditions.clear()
                            addNewCondition()
                            parseTransactionDefinitionModel.item = ParseTransactionDefinition()
                            newTransactionRuleAccessor.value = null
//                            newTransactionRule.value = true
                            ruleListView.selectionModel.clearSelection()
                            ruleListView.refresh()
                        }
                        button("Save rule") {
                            //                            textProperty.bind(Bindings.createStringBinding(Callable { if (newTransactionRule.value) "Create" else "Change" }, newTransactionRule))
                            textProperty.bind(Bindings.createStringBinding(Callable { if (newTransactionRuleAccessor.value == null) "Create" else "Change" }, newTransactionRuleAccessor))
                        }.action {
                            parseTransactionDefinitionModel.commit()
                            val newConditionDSO = ParseCondition().toDSO()
//                            if (newTransactionRule.value) {
                            if (newTransactionRuleAccessor.value == null) {
                                parserModel.rules.value = listOf(*parserModel.rules.value.toTypedArray(), ParserRule(listOf(*conditions.filterNot { it.toDSO() == newConditionDSO }.toTypedArray()), parseTransactionDefinitionModel.item)).observable()
                            } else {
                                newTransactionRuleAccessor.value!!.invoke().conditions = listOf(*conditions.filterNot { it.toDSO() == newConditionDSO }.toTypedArray()).observable()
                            }
                            conditions.clear()
                            addNewCondition()
                            parseTransactionDefinitionModel.item = ParseTransactionDefinition()
//                            newTransactionRule.value = true
                            newTransactionRuleAccessor.value = null
                            ruleListView.selectionModel.clearSelection()
                            ruleListView.refresh()
                        }
                    }
                }
            }
        }
    }

    init {
        rawData.onChange { updateSpreadsheetView() }
        parserModel.lineSeparator.onChange { updateSpreadsheetView() }
        parserModel.cellSeparator.onChange { updateSpreadsheetView() }
        spreadsheetView.isShowColumnHeader = false
        spreadsheetView.selectionModel.selectedCells.onChange {
            onCellPressOperation(it.list.map { pos -> pos.column to spreadsheetView.items[pos.row][pos.column].text })
        }
        conditions.onChange { addNewCondition() }
        addNewCondition()
    }

    private fun addNewCondition() {
        val newCondition = ParseCondition()
        if (conditions.isEmpty() || conditions.none { it.toDSO() == newCondition.toDSO() }) {
            conditions.add(newCondition)
            newCondition.valueProperty.onChange { addNewCondition() }
            newCondition.cellIndexProperty.onChange { addNewCondition() }
            newCondition.operationProperty.onChange { addNewCondition() }
            conditionsTable.refresh()
        }
    }

    private fun updateSpreadsheetView() {
        if (rawData.value != null) {
            runLater {
                val data = rawData.value
                        .split(if (parserModel.lineSeparator.value != "") parserModel.lineSeparator.value else "\n")
                        .map { it.split(if (parserModel.cellSeparator.value != "") parserModel.cellSeparator.value else ",") }
                val columns = data.map { it.size }.max() ?: 1
                spreadsheetView.columns.take(columns).forEach { spreadsheetView.showColumn(it) }
                spreadsheetView.columns.drop(columns).forEach { spreadsheetView.hideColumn(it) }
                grid.setRows(data.mapIndexed { rowNum, dataRow ->
                    (0..maxColumns)
                            .map { columnNum -> SpreadsheetCellType.STRING.createCell(rowNum, columnNum, 1, 1, dataRow.getOrNull(columnNum)) }
                            .observable()
                })
                spreadsheetView.columns.take(columns).forEach { it.fitColumn() }
            }
        }
    }
}
