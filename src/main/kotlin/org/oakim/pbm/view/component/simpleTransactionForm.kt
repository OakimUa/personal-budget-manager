package org.oakim.pbm.view.component

import javafx.beans.property.SimpleDoubleProperty
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

fun EventTarget.simpleTransactionForm(transactionType: TransactionType) = form {
    val amount = SimpleDoubleProperty(0.0)
    val wallet = SimpleObjectProperty(LocalStorage.wallets.firstOrNull { !it.archived })
    val tags = mutableListOf<Tag>().observable()
    val notes = SimpleStringProperty()
    val context = ValidationContext()
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
                            type = transactionType,
                            tags = setOf(*tags.toTypedArray()),
                            wallet = wallet.value!!,
                            reference = null,
                            notes = notes.value)
                    amount.value = 0.0
                    notes.value = null
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