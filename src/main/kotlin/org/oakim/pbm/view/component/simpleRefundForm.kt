package org.oakim.pbm.view.component

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.TextField
import org.oakim.pbm.app.Selector
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.service.LocalStorage
import tornadofx.*
import java.time.LocalDate
import java.time.Month

fun EventTarget.simpleRefundForm() = form {
    val amount = SimpleDoubleProperty(0.0)
    val refTransaction = SimpleObjectProperty<Transaction?>(null)
    val wallet = SimpleObjectProperty(LocalStorage.wallets.firstOrNull { !it.archived })
    val tags = mutableListOf<Tag>().observable()
    val notes = SimpleStringProperty()
    val context = ValidationContext()

    refTransaction.onChange { reference ->
        wallet.value = reference?.wallet
        tags.clear()
        tags.addAll(reference?.tags ?: setOf())
        notes.value = "[Refund] ${reference?.notes ?: ""}"
    }

    fieldset {
        var amountTF: TextField? = null
        var saveBtn: Button? = null
        field("Amount") {
            amountTF = textfield(amount) {
                context.addValidator(this, amount) { if (it!!.toDouble() > 0.0) null else error("Transaction amount should be positive") }
                action {
                    saveBtn?.requestFocus()
                }
            }
        }
        field("Transaction") {
            val refYear = SimpleIntegerProperty(if (LocalDate.now().month == Month.JANUARY) LocalDate.now().year - 1 else LocalDate.now().year)
            val refMonth = SimpleObjectProperty(LocalDate.now().month.minus(1))
            hbox(10) {
                combobox(refMonth, Month.values().toList().observable()) { bindSelected(refMonth) }
                combobox(refYear, (2010..LocalDate.now().year).toList().reversed().observable()) { bindSelected(refYear) }
                combobox(refTransaction, mutableListOf<Transaction>().observable()) {
                    items.addAll(LocalStorage.transactions[refYear.value, refMonth.value].filter { it.type == TransactionType.SPENDING })
                    refYear.onChange { year ->
                        println("Selected year: $year")
                        refTransaction.value = null
                        items.clear()
                        items.addAll(LocalStorage.transactions[year, refMonth.value].filter { it.type == TransactionType.SPENDING })
                    }
                    refMonth.onChange { month ->
                        println("Selected month: $month")
                        refTransaction.value = null
                        items.clear()
                        items.addAll(LocalStorage.transactions[refYear.value, month!!].filter { it.type == TransactionType.SPENDING })
                    }
                    cellFormat(FX.defaultScope) { transaction -> graphic = transaction?.asOneCell() }
                }
            }
        }
        field("Wallet") {
            combobox(wallet, LocalStorage.wallets.filterNot { it.archived }).cellFormat(FX.defaultScope) { graphic = it?.displayNode }
        }
        flowpane {
            LocalStorage.tagAspects.forEach { aspect ->
                hbox(10) {
                    label(aspect.let { if (it.isNotBlank()) it else "</>" }) {
                        addClass(Styles.labelInRow)
                    }
                    segmentedPBMEntityMultiSelector(
                            selected = tags,
                            values = LocalStorage.tags,
                            filterExtension = { it.aspect == aspect })
                }
            }
        }
        field(forceLabelIndent = true) {
            saveBtn = button("Save") {
                enableWhen(context.valid)
                action {
                    LocalStorage.transactions[Selector.selectedYear.value, Selector.selectedMonth.value] += Transaction.create(
                            amount = amount.value,
                            type = TransactionType.REFUND,
                            tags = setOf(*tags.toTypedArray()),
                            wallet = wallet.value!!,
                            reference = refTransaction.value?.id,
                            notes = notes.value)
                    amount.value = 0.0
                    refTransaction.value = null
                    amountTF?.requestFocus()
                    context.validate(decorateErrors = false)
                }
            }
        }
        field("Notes") {
            textarea(notes)
        }
    }
    context.validate(decorateErrors = false)
}