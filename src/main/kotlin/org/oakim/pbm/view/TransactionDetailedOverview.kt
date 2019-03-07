package org.oakim.pbm.view

import javafx.collections.ObservableList
import javafx.scene.paint.Color
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Selector
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.service.LocalStorage
import org.oakim.pbm.view.component.segmentedMultiSelector
import org.oakim.pbm.view.component.segmentedPBMEntityMultiSelector
import org.oakim.pbm.view.component.transactionTable
import tornadofx.*

class TransactionDetailedOverview : View("Detailed", Glyph("FontAwesome", FontAwesome.Glyph.TH_LIST)) {
    private val transactionList: SortedFilteredList<Transaction> = SortedFilteredList()

    private val selectedWallets: ObservableList<Wallet> by lazy {
        mutableListOf<Wallet>().observable().apply {
            addAll(LocalStorage.wallets)
            onChange { transactionList.refilter() }
        }
    }
    private val selectedTags: ObservableList<Tag> by lazy {
        mutableListOf<Tag>().observable().apply {
            addAll(LocalStorage.tags)
            onChange { transactionList.refilter() }
        }
    }
    private val selectedTypes: ObservableList<TransactionType> by lazy {
        mutableListOf<TransactionType>().observable().apply {
            addAll(TransactionType.values())
            onChange { transactionList.refilter() }
        }
    }

    override val root = borderpane {
        top = vbox(10) {
            addClass(Styles.content)
            hbox(10) {
                label("Wallet")
                segmentedPBMEntityMultiSelector(
                        selected = selectedWallets,
                        values = LocalStorage.wallets)
                label("Type")
                segmentedMultiSelector(
                        selected = selectedTypes,
                        values = TransactionType.values().toList().observable(),
                        buttonDecorator = { graphic = it.displayGlyph; text = "" })
            }
            flowpane {
                LocalStorage.tagAspects.forEach { aspect ->
                    hbox(10) {
                        label(aspect.let { if (it.isNotBlank()) it else "</>" }) {
                            addClass(Styles.labelInRow)
                        }
                        segmentedPBMEntityMultiSelector(
                                selected = selectedTags,
                                values = LocalStorage.tags,
                                filterExtension = { it.aspect == aspect })
                    }
                }
            }
        }
        center = transactionTable(transactionList)
    }

    override fun onTabSelected() {
        refreshData()
        super.onTabSelected()
    }

    private fun refreshData() {
        runLater {
            transactionList.clear()
            val elements = LocalStorage.transactions[Selector.selectedYear.value, Selector.selectedMonth.value]
            transactionList.addAll(elements)
        }
    }

    init {
        this.disableClose()
        Selector.selectedYear.onChange { refreshData() }
        Selector.selectedMonth.onChange { refreshData() }
        transactionList.predicate = { transaction ->
            transaction.wallet in selectedWallets &&
                    transaction.type in selectedTypes &&
                    (transaction.tags.isEmpty() || transaction.tags.any { it in selectedTags })
        }
    }
}
