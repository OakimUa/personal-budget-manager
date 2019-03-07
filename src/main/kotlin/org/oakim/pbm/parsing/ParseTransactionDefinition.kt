package org.oakim.pbm.parsing

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.data.Wallet
import tornadofx.*

class ParseTransactionDefinition(
        type: TransactionType = TransactionType.SPENDING,
        amountCell: Int = 0,
        invertAmount: Boolean = false,
        tags: List<Tag> = listOf(),
        notesCells: List<Int> = listOf(),
        targetWallet: Wallet? = null) {
    val typeProperty = SimpleObjectProperty<TransactionType>(this, "type", type)
    var type: TransactionType by typeProperty
    val amountCellProperty = SimpleIntegerProperty(this, "amountCell", amountCell)
    var amountCell: Int by amountCellProperty
    val invertAmountProperty = SimpleBooleanProperty(this, "invertAmount", invertAmount)
    var invertAmount: Boolean by invertAmountProperty
    val tagsProperty = SimpleListProperty<Tag>(this, "tags", tags.toMutableList().observable())
    var tags: ObservableList<Tag> by tagsProperty
    val notesCellsProperty = SimpleListProperty<Int>(this, "notesCells", notesCells.toMutableList().observable())
    var notesCells: ObservableList<Int> by notesCellsProperty
    val targetWalletProperty = SimpleObjectProperty<Wallet>(this, "targetWallet", targetWallet)
    var targetWallet: Wallet? by targetWalletProperty

    class DSO @JsonCreator constructor(
            @JsonProperty("type") val type: TransactionType,
            @JsonProperty("amount_cell") val amountCell: Int,
            @JsonProperty("invert_amount") val invertAmount: Boolean? = null,
            @JsonProperty("tags") val tags: List<String>? = null,
            @JsonProperty("notes_cells") val notesCells: List<Int>? = null,
            @JsonProperty("target_wallet") val targetWallet: String? = null) {
        fun toParseTransactionDefinition() = ParseTransactionDefinition(type, amountCell,
                invertAmount ?: false, tags?.mapNotNull(Tag.Companion::fromValue) ?: listOf(),
                notesCells ?: listOf(), Wallet.fromName(targetWallet))
    }

    fun toDSO() = DSO(type, amountCell,
            if (invertAmount) invertAmount else null,
            tags.map(Tag::value).let { if (it.isEmpty()) null else it },
            notesCells.let { if (it.isEmpty()) null else it },
            targetWallet?.name)

    class Model(parseTransactionDefinition: ParseTransactionDefinition) : ItemViewModel<ParseTransactionDefinition>(parseTransactionDefinition) {
        val type = bind(ParseTransactionDefinition::typeProperty)
        val amountCell = bind(ParseTransactionDefinition::amountCellProperty)
        val invertAmount = bind(ParseTransactionDefinition::invertAmountProperty)
        val tags = bind(ParseTransactionDefinition::tagsProperty)
        val notesCells = bind(ParseTransactionDefinition::notesCellsProperty)
        val targetWallet = bind(ParseTransactionDefinition::targetWalletProperty)
    }

    fun toTransactions(row: List<String>, toDouble: (String) -> Double?, wallet: Wallet): List<Transaction> {
        val amount = toDouble(row[amountCell])!!.let { if (invertAmount) -it else it }
        val setOfTags = setOf(*tags.toTypedArray())
        val notes = notesCells.mapNotNull { row.getOrNull(it) }.filterNot { it.isBlank() }.joinToString(", ")
        return when (type) {
            TransactionType.TRANSFER_IN, TransactionType.TRANSFER_OUT -> Transaction.transferPair(
                    amount = amount,
                    tags = setOfTags,
                    walletFrom = wallet,
                    walletTo = targetWallet ?: wallet,
                    notes = notes)
            // todo: REFUND
            else -> listOf(Transaction.create(
                    amount = amount,
                    type = type,
                    tags = setOfTags,
                    wallet = wallet,
                    notes = notes,
                    reference = null))
        }
    }

    fun copy() = ParseTransactionDefinition(type, amountCell, invertAmount,
            listOf(*tags.toTypedArray()), listOf(*notesCells.toTypedArray()), targetWallet)
}
