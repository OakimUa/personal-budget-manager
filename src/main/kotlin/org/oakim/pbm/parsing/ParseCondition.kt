package org.oakim.pbm.parsing

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class ParseCondition(
        cellIndex: Int = 0,
        operation: MatchingOperation = MatchingOperation.EQ,
        value: String = "") {
    val cellIndexProperty = SimpleIntegerProperty(this, "cellIndex", cellIndex)
    var cellIndex: Int by cellIndexProperty
    val operationProperty = SimpleObjectProperty<MatchingOperation>(this, "operation", operation)
    var operation: MatchingOperation by operationProperty
    val valueProperty = SimpleStringProperty(this, "value", value)
    var value: String by valueProperty

    data class DSO @JsonCreator constructor(
            @JsonProperty("cell_index") val cellIndex: Int,
            @JsonProperty("operation") val operation: MatchingOperation,
            @JsonProperty("value") val value: String) {
        fun toParseCondition() = ParseCondition(cellIndex, operation, value)
    }

    fun toDSO() = DSO(cellIndex, operation, value)

    fun asLabelText() = when(operation) {
        MatchingOperation.EQ -> "text at $cellIndex = '$value'"
        MatchingOperation.CONTAINS -> "text at $cellIndex contains '$value'"
        MatchingOperation.NUMERIC_POSITIVE -> "number at $cellIndex is positive"
        MatchingOperation.NUMERIC_NEGATIVE -> "number at $cellIndex is negative"
    }

    class Model(parseCondition: ParseCondition) : ItemViewModel<ParseCondition>(parseCondition) {
        val cellIndex = bind(ParseCondition::cellIndexProperty)
        val operation = bind(ParseCondition::operationProperty)
        val value = bind(ParseCondition::valueProperty)
    }

    fun matches(row: List<String>, toDouble: (String) -> Double?): Boolean =
            row.getOrNull(cellIndex)?.let { cellValue ->
                when (operation) {
                    MatchingOperation.EQ -> cellValue.equals(value, true)
                    MatchingOperation.CONTAINS -> cellValue.contains(value, true)
                    MatchingOperation.NUMERIC_POSITIVE -> toDouble(cellValue)?.let { it >= 0 } == true
                    MatchingOperation.NUMERIC_NEGATIVE -> toDouble(cellValue)?.let { it <= 0 } == true
                }
            } == true

    fun copy() = ParseCondition(cellIndex, operation, value)
}
