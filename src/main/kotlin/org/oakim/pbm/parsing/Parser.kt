package org.oakim.pbm.parsing

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.service.LocalStorage
import org.oakim.pbm.util.LOG
import tornadofx.*


class Parser(
        name: String = "New Parser",
        lineSeparator: String = "\n",
        cellSeparator: String = ",",
        decimalPoint: String = ".",
        thousandSeparator: String = ",",
        wallet: Wallet? = LocalStorage.wallets.firstOrNull(),
        rules: List<ParserRule> = listOf()) {
    val nameProperty = SimpleStringProperty(this, "name", name)
    var name: String by nameProperty
    val lineSeparatorProperty = SimpleStringProperty(this, "lineSeparator", lineSeparator)
    var lineSeparator: String by lineSeparatorProperty
    val cellSeparatorProperty = SimpleStringProperty(this, "cellSeparator", cellSeparator)
    var cellSeparator: String by cellSeparatorProperty
    val decimalPointProperty = SimpleStringProperty(this, "decimalPoint", decimalPoint)
    var decimalPoint: String by decimalPointProperty
    val thousandSeparatorProperty = SimpleStringProperty(this, "thousandSeparator", thousandSeparator)
    var thousandSeparator: String by thousandSeparatorProperty
    val walletProperty = SimpleObjectProperty<Wallet>(this, "wallet", wallet)
    var wallet: Wallet by walletProperty
    val rulesProperty = SimpleListProperty(this, "rules", rules.toMutableList().observable())
    var rules: ObservableList<ParserRule> by rulesProperty

    data class DSO @JsonCreator constructor(
            @JsonProperty("name") val name: String,
            @JsonProperty("line_separator") val lineSeparator: String,
            @JsonProperty("cell_separator") val cellSeparator: String,
            @JsonProperty("decimal_point") val decimalPoint: String,
            @JsonProperty("thousand_separator") val thousandSeparator: String,
            @JsonProperty("wallet") val wallet: String,
            @JsonProperty("rules") val rules: List<ParserRule.DSO>) {
        fun toParser() = Parser(name, lineSeparator, cellSeparator, decimalPoint, thousandSeparator, Wallet.fromName(wallet), rules.map(ParserRule.DSO::toParserRule))
    }

    fun toDSO() = DSO(name, lineSeparator, cellSeparator, decimalPoint, thousandSeparator, wallet.name, rules.map(ParserRule::toDSO))

    class Model(parser: Parser) : ItemViewModel<Parser>(parser) {
        val name = bind(Parser::nameProperty)
        val lineSeparator = bind(Parser::lineSeparatorProperty)
        val cellSeparator = bind(Parser::cellSeparatorProperty)
        val decimalPoint = bind(Parser::decimalPointProperty)
        val thousandSeparator = bind(Parser::thousandSeparatorProperty)
        val wallet = bind(Parser::walletProperty)
        val rules = bind(Parser::rulesProperty)
    }

    val toDoubleFn: (String) -> Double? = { it.replace(thousandSeparator, "").replace(decimalPoint, ".").toDoubleOrNull() }
    fun parse(payload: String) =
            payload.split(lineSeparator)
                    .map { it.split(cellSeparator) }
                    .flatMap { row ->
                        LOG.debug("Parsing row: ${row.joinToString("|")}")
                        rules.firstOrNull { it.matches(row, toDoubleFn) }?.transform(row, toDoubleFn, wallet)
                                ?: listOf()
                    }

    fun testParse(payload: String) =
            payload.split(lineSeparator)
                    .map { it.split(cellSeparator) }
                    .flatMap { row ->
                        LOG.debug("Parsing row: ${row.joinToString("|")}")
                        rules.firstOrNull { it.matches(row, toDoubleFn) }?.let { rule -> rule.transform(row, toDoubleFn, wallet).map { rule to it } }
                                ?: listOf()
                    }
}
