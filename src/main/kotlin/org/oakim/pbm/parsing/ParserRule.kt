package org.oakim.pbm.parsing

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.util.LOG
import tornadofx.*

class ParserRule(
        conditions: List<ParseCondition> = listOf(),
        definition: ParseTransactionDefinition = ParseTransactionDefinition()) {
    val conditionsProperty = SimpleListProperty(this, "conditions", conditions.toMutableList().observable())
    var conditions: ObservableList<ParseCondition> by conditionsProperty
    val definitionProperty = SimpleObjectProperty<ParseTransactionDefinition>(this, "definition", definition)
    var definition: ParseTransactionDefinition by definitionProperty

    data class DSO @JsonCreator constructor(
            @JsonProperty("conditions") val conditions: List<ParseCondition.DSO>,
            @JsonProperty("definition") val definition: ParseTransactionDefinition.DSO) {
        fun toParserRule() = ParserRule(conditions.map(ParseCondition.DSO::toParseCondition), definition.toParseTransactionDefinition())
    }

    fun toDSO() = DSO(conditions.map(ParseCondition::toDSO), definition.toDSO())
    class Model(parserRule: ParserRule) : ItemViewModel<ParserRule>(parserRule) {
        val conditions = bind(ParserRule::conditionsProperty)
        val definition = bind(ParserRule::definitionProperty)
    }

    fun matches(row: List<String>, toDouble: (String) -> Double?): Boolean {
        LOG.debug("  -> test for ${definition.type} at ${definition.amountCell}")
        return conditions.all { condition ->
            condition.matches(row, toDouble).also {
                LOG.debug("    -> ${condition.asLabelText()} => $it")
            }
        }
    }

    fun transform(row: List<String>, toDouble: (String) -> Double?, wallet: Wallet): List<Transaction> =
            definition.toTransactions(row, toDouble, wallet)

    fun copy() = ParserRule(
            conditions = this.conditions.map(ParseCondition::copy),
            definition = this.definition.copy())

}
