package org.oakim.pbm.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableSet
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Selector
import org.oakim.pbm.service.LocalStorage
import tornadofx.*
import java.time.Month
import kotlin.math.roundToInt

class Transaction private constructor(id: TransactionID = TransactionID.new(),
                                      amount: Double,
                                      type: TransactionType,
                                      tags: Set<Tag>,
                                      wallet: Wallet,
                                      reference: TransactionID? = null,
                                      notes: String?) {
    val idProperty = SimpleObjectProperty<TransactionID>(this, "id", id)
    var id: TransactionID by idProperty

    val amountProperty = SimpleDoubleProperty(this, "amount", amount)
    var amount: Double by amountProperty

    val typeProperty = SimpleObjectProperty<TransactionType>(this, "type", type)
    var type: TransactionType by typeProperty

    val tagsProperty = SimpleSetProperty<Tag>(this, "tags", tags.observable())
    var tags: ObservableSet<Tag> by tagsProperty

    val walletProperty = SimpleObjectProperty<Wallet>(this, "wallet", wallet)
    var wallet: Wallet by walletProperty

    val referenceProperty = SimpleObjectProperty(this, "reference", reference)
    var reference: TransactionID? by referenceProperty

    val notesProperty = SimpleStringProperty(this, "notes", notes)
    var notes: String by notesProperty

    companion object {
        fun create(year: Int = Selector.selectedYear.value,
                   month: Month = Selector.selectedMonth.value,
                   amount: Double, // mandatory
                   type: TransactionType, // TODO: selector
                   tags: Set<Tag>, // mandatory
                   wallet: Wallet, // TODO: selector
                   reference: TransactionID?, // mandatory
                   notes: String?): Transaction =
                Transaction(
                        id = TransactionID.new(year, month),
                        amount = amount,
                        type = type,
                        tags = tags,
                        wallet = wallet,
                        reference = reference,
                        notes = notes)

        fun transferPair(year: Int = Selector.selectedYear.value,
                         month: Month = Selector.selectedMonth.value,
                         amount: Double,
                         walletFrom: Wallet,
                         walletTo: Wallet,
                         tags: Set<Tag> = setOf(),
                         notes: String?): List<Transaction> {
            val trIdFrom = TransactionID.new(year, month)
            val trIdTo = TransactionID.new(year, month)
            return listOf(
                    Transaction(
                            id = trIdFrom,
                            amount = amount,
                            type = TransactionType.TRANSFER_OUT,
                            tags = tags,
                            wallet = walletFrom,
                            reference = trIdTo,
                            notes = notes),
                    Transaction(
                            id = trIdTo,
                            amount = amount,
                            type = TransactionType.TRANSFER_IN,
                            tags = tags,
                            wallet = walletTo,
                            reference = trIdFrom,
                            notes = notes)
            )
        }

    }

    data class DSO @JsonCreator constructor(
            @JsonProperty("id") val id: String,
            @JsonProperty("amount") val amount: Int,
            @JsonProperty("type") val type: TransactionType,
            @JsonProperty("tags") val tags: Set<String>?,
            @JsonProperty("wallet") val wallet: String,
            @JsonProperty("reference") val reference: String?,
            @JsonProperty("notes") val notes: String?) {
        fun toTransaction(): Transaction = Transaction(
                id = TransactionID.fromRef(id),
                amount = amount.toDouble() / 100,
                type = type,
                tags = tags?.let { tagNames -> LocalStorage.tags.filter { it.value in tagNames }.toSet() } ?: setOf(),
                wallet = LocalStorage.wallets.first { it.name == wallet },
                reference = if (reference == null) null else TransactionID.fromRef(reference),
                notes = notes)
    }

    fun toDSO(): DSO = DSO(
            id = id.toRef(),
            amount = (amount * 100).roundToInt(),
            type = type,
            tags = tags.map(Tag::value).toSet(),
            wallet = wallet.name,
            reference = reference?.toRef(),
            notes = notes)

    fun asOneCell(): Node = HBox(5.0).apply {
        this += type.displayGlyph
        this += label(amount.toString())
        when (type) {
            TransactionType.REFUND -> {
                this += Label("[")
                this += reference?.transaction()?.asOneCell() ?: Label("?")
                this += Label("]")
                this += wallet.displayNode
                if (!notes.isNullOrBlank())
                    this += Label("/$notes/")
            }
            TransactionType.TRANSFER_OUT -> {
                this += wallet.displayNode
                this += Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT)
                this += reference?.transaction()?.wallet?.displayNode ?: Label("?")
            }
            TransactionType.TRANSFER_IN -> {
                this += reference?.transaction()?.wallet?.displayNode ?: Label("?")
                this += Glyph("FontAwesome", FontAwesome.Glyph.ARROW_RIGHT)
                this += wallet.displayNode
            }
            else -> {
                this += wallet.displayNode
                if (!notes.isNullOrBlank())
                    this += Label("/$notes/")
            }
        }
    }
}
