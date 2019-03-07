package org.oakim.pbm.parsing

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.service.LocalStorage
import tornadofx.*
import java.io.BufferedReader
import java.io.File
import java.time.Month

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParserTest {
    private val json: ObjectMapper by lazy {
        jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    val yaml: ObjectMapper by lazy {
        ObjectMapper(YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE)
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    @Test
    fun deutscheBankParser() {
//        val io = File("/Users/ilomonos/Documents/workspace-personal/personal-budget-manager/src/test/resources/Transactions_704_195048400_20190119_165401-one-line.csv").inputStream()
////        val io = File("/Users/ilomonos/Documents/workspace-personal/personal-budget-manager/src/test/resources/Transactions_704_195048400_20190119_165401.csv").inputStream()
//        val br = io.bufferedReader()
//        val parser = Parser(
//                "DB Test",
////                ParserType.CSV,
//                "\n",
//                ";",
//                ".",
//                ",",
//                listOf(
//                        ParserRule(conditions = listOf(ParseCondition(0, MatchingOperation.EQ, "Old balance:")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.PERIOD_START,
//                                        amountCell = 4)),
//                        ParserRule(conditions = listOf(ParseCondition(16, MatchingOperation.IS, "numeric,positive"), ParseCondition(4, MatchingOperation.CONTAINS, "Lohn/Gehalt")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.INCOME,
//                                        amountCell = 16,
////                                        tags = listOf("Salary"),
//                                        notesCells = listOf(3, 4, 5, 6, 7))),
//                        ParserRule(conditions = listOf(ParseCondition(16, MatchingOperation.IS, "numeric,positive")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.INCOME,
//                                        amountCell = 16,
//                                        notesCells = listOf(3, 4, 5, 6, 7))),
//                        ParserRule(conditions = listOf(ParseCondition(15, MatchingOperation.IS, "numeric,negative"), ParseCondition(4, MatchingOperation.EQ, "DE88500700100175526303")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.TRANSFER_OUT,
//                                        amountCell = 15, amountOp = "invert",
////                                        targetWallet = "PayPal",
//                                        notesCells = listOf(3, 4, 5, 6, 7))),
//                        ParserRule(conditions = listOf(ParseCondition(15, MatchingOperation.IS, "numeric,negative"), ParseCondition(3, MatchingOperation.CONTAINS, "DANKE, IHR LIDL")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.SPENDING,
//                                        amountCell = 15, amountOp = "invert",
////                                        tags = listOf("Супермаркет", "Быт", "Lidl"),
//                                        notesCells = listOf(3, 4, 5, 6, 7))),
//                        ParserRule(conditions = listOf(ParseCondition(15, MatchingOperation.IS, "numeric,negative")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.SPENDING,
//                                        amountCell = 15, amountOp = "invert",
//                                        notesCells = listOf(3, 4, 5, 6, 7))),
//                        ParserRule(conditions = listOf(ParseCondition(0, MatchingOperation.EQ, "Account balance")),
//                                definition = ParseTransactionDefinition(
//                                        type = TransactionType.PERIOD_END,
//                                        amountCell = 4))
//                ))
//        println(yaml.writeValueAsString(parser))

//        process(br, parser).forEach {
//            println("${it.type} ${it.amount} ${it.wallet.name} ${it.tags.map(Tag::value)} ${it.notes}")
//        }
    }

//    private fun process(bufferedReader: BufferedReader, parser: Parser) =
//            bufferedReader.readText().split(parser.lineSeparator)
//                    .flatMap { line -> process(line, parser) }
//
//    private fun process(line: String, parser: Parser): List<Transaction> =
//            preprocess(line, parser)
//                    .let { cells ->
//                        parser.rules.firstOrNull { rule -> test(cells, rule, parser) }
//                                ?.definition
//                                ?.let { def -> transform(cells, def, parser) }
//                                ?: listOf()
//                    }
//
//    private fun transform(cells: List<String>, def: ParseTransactionDefinition, parser: Parser): List<Transaction> =
//            when (def.type) {
//                TransactionType.TRANSFER_OUT ->
//                    (cells.getOrNull(def.amountCell)
//                            ?.replace(parser.thousandSeparator, "")
//                            ?.replace(parser.decimalPoint, ".")
//                            ?.toDoubleOrNull()
//                            ?.let { rawAmount ->
//                                when (def.amountOp) {
//                                    "invert" -> -rawAmount
//                                    else -> rawAmount
//                                }
//                            } ?: 0.0).let { amount ->
//                        listOf(Transaction.create(
//                                2010,
//                                Month.JANUARY,
//                                amount,
//                                TransactionType.TRANSFER_OUT,
//                                def.tags?.mapNotNull { tagName -> LocalStorage.tags.firstOrNull { tag -> tag.value == tagName } }?.toSet()
//                                        ?: setOf(),
//                                Wallet(name = "test"),
//                                null,
//                                def.notesCells?.map { index -> cells.getOrNull(index) }?.filterNot { it.isNullOrBlank() }?.joinToString("; ")
//                        ), Transaction.create(
//                                2010,
//                                Month.JANUARY,
//                                -amount,
//                                TransactionType.TRANSFER_IN,
//                                def.tags?.mapNotNull { tagName -> LocalStorage.tags.firstOrNull { tag -> tag.value == tagName } }?.toSet()
//                                        ?: setOf(),
//                                Wallet(name = def.targetWallet ?: "UNKNOWN"),
//                                null,
//                                def.notesCells?.map { index -> cells.getOrNull(index) }?.filterNot { it.isNullOrBlank() }?.joinToString("; ")
//                        ))
//                    }
//                else -> listOf(Transaction.create(
//                        2010,
//                        Month.JANUARY,
//                        cells.getOrNull(def.amountCell)
//                                ?.replace(parser.thousandSeparator, "")
//                                ?.replace(parser.decimalPoint, ".")
//                                ?.toDoubleOrNull()
//                                ?.let { rawAmount ->
//                                    when (def.amountOp) {
//                                        "invert" -> -rawAmount
//                                        else -> rawAmount
//                                    }
//                                } ?: 0.0,
//                        def.type,
//                        def.tags?.mapNotNull { tagName -> LocalStorage.tags.firstOrNull { tag -> tag.value == tagName } }?.toSet()
//                                ?: setOf(),
//                        Wallet(name = "test"),
//                        null,
//                        def.notesCells?.map { index -> cells.getOrNull(index) }?.filterNot { it.isNullOrBlank() }?.joinToString("; ")
//                ))
//            }
//
//
//    private fun test(cells: List<String>, rule: ParserRule, parser: Parser): Boolean =
//            rule.conditions.all { condition ->
//                !cells.getOrNull(condition.cellIndex).isNullOrBlank() &&
//                        test(cells[condition.cellIndex], condition.operation, condition.value, parser)
//            }
//
//    private fun test(cellValue: String, operation: MatchingOperation, expected: String, parser: Parser): Boolean = when (operation) {
//        MatchingOperation.EQ -> cellValue == expected
//        MatchingOperation.CONTAINS -> cellValue.contains(expected)
//        MatchingOperation.IS -> when (expected) {
//            "numeric" -> cellValue.replace(parser.thousandSeparator, "").replace(parser.decimalPoint, ".").isDouble()
//            "numeric,positive" -> (cellValue.replace(parser.thousandSeparator, "").replace(parser.decimalPoint, ".").toDoubleOrNull()?.compareTo(0)
//                    ?: 0) > 0
//            "numeric,negative" -> (cellValue.replace(parser.thousandSeparator, "").replace(parser.decimalPoint, ".").toDoubleOrNull()?.compareTo(0)
//                    ?: 0) < 0
//            else -> false
//        }
//    }
//
//    private fun preprocess(line: String, parser: Parser) = line.split(parser.cellSeparator)
}