package org.oakim.pbm.view.component

import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.event.EventTarget
import javafx.geometry.Pos
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.model.WalletModel
import org.oakim.pbm.service.LocalStorage
import tornadofx.*
import tornadofx.controlsfx.glyph
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.Callable

fun EventTarget.walletForm(model: WalletModel,
                           isNewWallet: BooleanProperty = SimpleBooleanProperty(true),
                           commitNew: (Wallet) -> Unit,
                           commitEdit: (Wallet) -> Unit): Form = form {
    val amount = SimpleDoubleProperty(0.0)
    val startYear = SimpleIntegerProperty(if (LocalDate.now().month == Month.JANUARY) LocalDate.now().year - 1 else LocalDate.now().year)
    val startMonth = SimpleObjectProperty(LocalDate.now().month.minus(1))
    val context = ValidationContext()
    addClass(Styles.content)
    fieldset {
        field("Glyph") {
            combobox(model.glyph, FontAwesome.Glyph.values().map { it.name }.observable()) {
                cellFormat(FX.defaultScope) {
                    graphic = label(item, graphic = Glyph("FontAwesome", FontAwesome.Glyph.valueOf(item)))
                }
            }
        }
        field("Name") {
            textfield(model.name) {
                disableProperty().bind(isNewWallet.not())
            }.required()
        }
        field("Tags") {
            segmentedPBMEntityMultiSelector(
                    selected = model.tags,
                    values = LocalStorage.walletTags)
        }
        field("Notes") {
            textarea(model.notes)
            useMaxWidth = true
            gridpaneConstraints {
                columnSpan = 3
            }
        }
        field("Init") {
            visibleWhen(isNewWallet)
            textfield(amount) {
                context.addValidator(this, amount) { if (it!!.toDouble() >= 0.0) null else error("Transaction amount should be positive") }
            }
            glyph("FontAwesome", FontAwesome.Glyph.AT) {}
            combobox(startMonth, Month.values().toList().observable()) { bindSelected(startMonth) }
            combobox(startYear, (2010..LocalDate.now().year).toList().reversed().observable()) { bindSelected(startYear) }
        }
        field(forceLabelIndent = true) {
            hbox(10, alignment = Pos.CENTER_RIGHT) {
                button("Add/Update") {
                    enableWhen(model.valid.and(context.valid.or(isNewWallet.not())))
                    textProperty().bind(Bindings.createStringBinding(Callable { if (isNewWallet.value) "Add" else "Update" }, isNewWallet))
                }.action {
                    model.commit()
                    if (isNewWallet.value) {
                        val newWallet = model.item
                        commitNew(newWallet)
                        model.item = Wallet()
                        LocalStorage.transactions[startYear.value, startMonth.value] += Transaction.create(
                                year = startYear.value,
                                month = startMonth.value,
                                amount = amount.value,
                                type = TransactionType.PERIOD_START,
                                tags = setOf(),
                                wallet = newWallet,
                                reference = null,
                                notes = "[Initial]")
                        amount.value = 0.0
                    } else {
                        commitEdit(model.item)
                        LocalStorage.storeWallets()
                    }
                }
                button("Reset").action {
                    model.rollback()
                    amount.value = 0.0
                }
                button("New").action {
                    model.rollback()
                    model.item = Wallet()
                    amount.value = 0.0
                    isNewWallet.value = true
                }
            }
            useMaxWidth = true
            gridpaneConstraints {
                columnSpan = 3
            }
        }
    }
    context.validate(decorateErrors = false)
    model.validate(decorateErrors = false)
}