package org.oakim.pbm.view.component

import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.TableView
import javafx.scene.control.ToggleButton
import javafx.scene.paint.Color
import org.controlsfx.control.SegmentedButton
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.data.Archivable
import org.oakim.pbm.data.NamedGlyph
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.parsing.ParserRule
import tornadofx.*
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun EventTarget.popInfo(text: String) =
        label(graphic = Glyph("FontAwesome", FontAwesome.Glyph.INFO_CIRCLE)) {
            tooltip(text)
        }

inline fun <reified T> EventTarget.segmentedPBMEntityMultiSelector(
        selected: ObservableList<T>,
        values: ObservableList<T>,
        crossinline filterExtension: (T) -> Boolean = { true }): SegmentedButton where T : NamedGlyph, T : Archivable =
        this.segmentedMultiSelector(
                selected = selected,
                values = values,
                filter = { !it.archived && filterExtension(it) },
                trackProperties = T::class.memberProperties.filter { it.name == "archivedProperty" }.map { it as KProperty1<T, Property<*>> }.toSet(),
                buttonDecorator = { item ->
                    textProperty().bind(item.nameProperty)
                    graphicProperty().bind(Bindings.createObjectBinding<Node>(Callable { item.displayGlyph }, item.glyphProperty))
                })

fun <T> EventTarget.segmentedMultiSelector(selected: ObservableList<T>,
                                           values: ObservableList<T>,
                                           filter: (T) -> Boolean = { true },
                                           trackProperties: Set<KProperty1<T, Property<*>>> = setOf(),
                                           buttonDecorator: ToggleButton.(T) -> Unit = {},
                                           op: SegmentedButton.() -> Unit = {}): SegmentedButton {
    val segmentedButton = SegmentedButton()

    fun List<T>.toToggleButtons(): List<ToggleButton> = map { item ->
        ToggleButton(item.toString()).apply {
            properties["custom-id"] = item.toString()
            this.buttonDecorator(item)
            isSelected = filter(item) && item in selected
            selected.onChange { isSelected = item in it.list }
            action {
                if (isSelected) {
                    if (item !in selected)
                        selected += item
                } else {
                    selected.removeIf { i -> i == item }
                }
            }
        }
    }

    fun List<T>.bindToButtons() {
        forEach { item ->
            trackProperties.forEach { prop ->
                prop.get(item).onChange {
                    if (filter(item)) {
                        segmentedButton.buttons.addAll(listOf(item).toToggleButtons())
                    } else {
                        segmentedButton.buttons.removeIf { btn -> btn.properties["custom-id"] == item.toString() }
                        selected.removeIf { i -> i == item }
                    }
                }
            }
        }
    }

    segmentedButton.buttons.addAll(values.filter(filter).toToggleButtons())
    values.onChange { change ->
        while (change.next()) {
            if (change.wasAdded()) {
                segmentedButton.buttons.addAll(change.addedSubList.filter(filter).toToggleButtons())
                change.addedSubList.bindToButtons()
            }
        }
    }
    values.bindToButtons()
    segmentedButton.toggleGroup = null
    segmentedButton.addClass(SegmentedButton.STYLE_CLASS_DARK)
    return opcr(this, segmentedButton, op)
}

inline fun <reified T> EventTarget.segmentedPBMEntityMultiSelector(
        selected: Property<ObservableSet<T>>,
        values: ObservableList<T>,
        crossinline filterExtension: (T) -> Boolean = { true }): SegmentedButton where T : NamedGlyph, T : Archivable =
        this.segmentedMultiSelector(
                selected = selected,
                values = values,
                filter = { !it.archived && filterExtension(it) },
                trackProperties = T::class.memberProperties.filter { it.name == "archivedProperty" }.map { it as KProperty1<T, Property<*>> }.toSet(),
                buttonDecorator = { item ->
                    textProperty().bind(item.nameProperty)
                    graphicProperty().bind(Bindings.createObjectBinding<Node>(Callable { item.displayGlyph }, item.glyphProperty))
                })

fun <T> EventTarget.segmentedMultiSelector(selected: Property<ObservableSet<T>>,
                                           values: ObservableList<T>,
                                           filter: (T) -> Boolean,
                                           trackProperties: Set<KProperty1<T, Property<*>>>,
                                           buttonDecorator: ToggleButton.(T) -> Unit = {},
                                           op: SegmentedButton.() -> Unit = {}): SegmentedButton {
    val segmentedButton = SegmentedButton()

    fun List<T>.toToggleButtons(): List<ToggleButton> = map { item ->
        ToggleButton(item.toString()).apply {
            properties["custom-id"] = item.toString()
            this.buttonDecorator(item)
            isSelected = filter(item) && item in selected.value
            selected.onChange { isSelected = it?.contains(item) == true }
            action {
                if (isSelected) {
                    if (item !in selected.value)
                        selected.value = (selected.value + item).toSet().observable()
                } else {
                    selected.value = selected.value.filterNot { i -> i == item }.toSet().observable()
                }
            }
        }
    }

    fun List<T>.bindToButtons() {
        forEach { item ->
            trackProperties.forEach { prop ->
                prop.get(item).onChange {
                    if (filter(item)) {
                        segmentedButton.buttons.addAll(listOf(item).toToggleButtons())
                    } else {
                        segmentedButton.buttons.removeIf { btn -> btn.properties["custom-id"] == item.toString() }
                        selected.value = selected.value.filterNot { i -> i == item }.toSet().observable()
                    }
                }
            }
        }
    }

    segmentedButton.buttons.addAll(values.filter(filter).toToggleButtons())
    values.onChange { change ->
        while (change.next()) {
            if (change.wasAdded()) {
                segmentedButton.buttons.addAll(change.addedSubList.filter(filter).toToggleButtons())
                change.addedSubList.bindToButtons()
            }
        }
    }
    values.bindToButtons()
    segmentedButton.toggleGroup = null
    segmentedButton.addClass(SegmentedButton.STYLE_CLASS_DARK)
    return opcr(this, segmentedButton, op)
}

fun EventTarget.comboWithField(property: Property<String>, proposed: Map<String, String>) = hbox(10) {
    val comboChoice = SimpleStringProperty(if (property.value in proposed.keys) property.value else "")
    combobox(comboChoice, listOf("", *proposed.keys.toTypedArray())).cellFormat(FX.defaultScope) { symbol: String ->
        text = when (symbol) {
            "" -> "Put symbol"
            else -> proposed[symbol] ?: "???"
        }
    }
    val block = AtomicBoolean(false)
    property.onChange {
        if (!block.get()) {
            block.set(true)
            comboChoice.value = if (it in proposed.keys) it else ""
            block.set(false)
        }
    }
    textfield(property) {
        enableWhen { comboChoice.isEqualTo("") }
        comboChoice.onChange { symbol ->
            if (symbol != null && symbol != "" && !block.get()) {
                block.set(true)
                property.value = symbol
                block.set(false)
            }
        }
    }
}

fun EventTarget.transactionTable(transactions: ObservableList<Transaction>, prefRows: Int = 30, op: TableView<Transaction>.() -> Unit = {}) = tableview(transactions) {
    fun color(transaction: Transaction) = when (transaction.type) {
        TransactionType.SPENDING -> Color.DARKRED
        TransactionType.INCOME -> Color.DARKGREEN
        TransactionType.TRANSFER_OUT -> Color.DARKRED
        TransactionType.TRANSFER_IN -> Color.DARKGREEN
        else -> Color.BLACK
    }
    readonlyColumn("", Transaction::type).cellFormat {
        graphic = it.displayGlyph.apply {
            setColor(color(rowItem))
        }
    }
    readonlyColumn("Amount", Transaction::amount).cellFormat {
        text = String.format("%.2f", it)
        textFill = color(rowItem)
    }
    readonlyColumn("Wallet", Transaction::wallet).cellFormat { graphic = it.displayNode }
    readonlyColumn("Tags", Transaction::tags).cellFormat { tags ->
        graphic = hbox(10) { tags.forEach { opcr(this, it.displayNode) } }
    }
    readonlyColumn("Notes", Transaction::notes).remainingWidth()
    fixedCellSize = 30.0
    prefHeightProperty().bind(fixedCellSizeProperty().multiply(prefRows).add(30))
    transactions.onChange { requestResize() }
    op(this)
}

fun EventTarget.testTransactionTable(transactions: ObservableList<Pair<ParserRule, Transaction>>, prefRows: Int = 30, op: TableView<Pair<ParserRule, Transaction>>.() -> Unit = {}) = tableview(transactions) {
    fun color(transaction: Transaction) = when (transaction.type) {
        TransactionType.SPENDING -> Color.DARKRED
        TransactionType.INCOME -> Color.DARKGREEN
        TransactionType.TRANSFER_OUT -> Color.DARKRED
        TransactionType.TRANSFER_IN -> Color.DARKGREEN
        else -> Color.BLACK
    }
    readonlyColumn("", Pair<ParserRule, Transaction>::second).cellFormat {
        graphic = it.type.displayGlyph.apply {
            setColor(color(rowItem.second))
        }
    }
    readonlyColumn("Amount", Pair<ParserRule, Transaction>::second).cellFormat {
        text = String.format("%.2f", it.amount)
        textFill = color(rowItem.second)
    }
    readonlyColumn("Wallet", Pair<ParserRule, Transaction>::second).cellFormat { graphic = it.wallet.displayNode }
    readonlyColumn("Tags", Pair<ParserRule, Transaction>::second).cellFormat { transaction ->
        graphic = hbox(10) { transaction.tags.forEach { opcr(this, it.displayNode) } }
    }
    readonlyColumn("Notes", Pair<ParserRule, Transaction>::second).apply { remainingWidth() }.cellFormat { this.text = it.notes }
    fixedCellSize = 30.0
    prefHeightProperty().bind(fixedCellSizeProperty().multiply(prefRows).add(30))
    transactions.onChange { requestResize() }
    op(this)
}


/*
Tags input

    val newStr = SimpleStringProperty()
    val tags = mutableListOf<String>().observable()
    customTextfield {
        bind(newStr)
        tags.onChange { change ->
            left = hbox(5) {
                change.list.forEach { elem ->
                    label(elem)
                }
            }
        }
        action {
            if (!newStr.value.isNullOrBlank()) {
                tags.add(newStr.value)
                newStr.value = ""
            }
        }
        bindAutoCompletion("Qwerty", "Asdfgh", "Qwedsa")
    }
 */