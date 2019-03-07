package org.oakim.pbm.app

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*
import java.time.LocalDate
import java.time.Month

object Selector {
    private val currentYear = if (LocalDate.now().month == Month.JANUARY) LocalDate.now().year - 1 else LocalDate.now().year
    val selectedYear = SimpleIntegerProperty(currentYear)
    val yearList = (2010..LocalDate.now().year).toList().reversed().observable()
    val selectedMonth = SimpleObjectProperty(LocalDate.now().month.minus(1))
    val hasChanges = SimpleBooleanProperty(false)
}