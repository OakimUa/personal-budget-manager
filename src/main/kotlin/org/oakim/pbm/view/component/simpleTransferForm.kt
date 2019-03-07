package org.oakim.pbm.view.component

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.TextField
import org.controlsfx.glyphfont.FontAwesome
import org.oakim.pbm.app.Selector
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.service.LocalStorage
import tornadofx.*
import tornadofx.controlsfx.glyph

fun EventTarget.simpleTransferForm() = form {
    val amount = SimpleDoubleProperty(0.0)
    val walletFrom = SimpleObjectProperty(LocalStorage.wallets.firstOrNull { !it.archived })
    val walletTo = SimpleObjectProperty(LocalStorage.wallets.filterNot { it.archived }.drop(1).firstOrNull())
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
        field("Wallets") {
            hbox(10) {
                combobox(walletFrom, LocalStorage.wallets.filterNot { it.archived }) {
                    context.addValidator(this, walletFrom) { if (it == null) error("Wallet should be selected") else null }
                    cellFormat(FX.defaultScope) { graphic = it?.displayNode }
                }
                glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT) {}
                combobox(walletTo, LocalStorage.wallets.filterNot { it.archived }) {
                    context.addValidator(this, walletTo) { if (it == null) error("Wallet should be selected") else null }
                    cellFormat(FX.defaultScope) { graphic = it?.displayNode }
                }
            }
        }
        field(forceLabelIndent = true) {
            saveBtn = button("Save") {
                enableWhen(context.valid)
                action {
                    LocalStorage.transactions[Selector.selectedYear.value, Selector.selectedMonth.value] += Transaction.transferPair(
                            amount = amount.value,
                            tags = setOf(),
                            walletFrom = walletFrom.value!!,
                            walletTo = walletTo.value!!,
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