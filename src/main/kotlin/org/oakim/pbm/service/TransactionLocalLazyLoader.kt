package org.oakim.pbm.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import org.oakim.pbm.data.Transaction
import org.oakim.pbm.data.TransactionID
import tornadofx.*
import java.io.File
import java.time.Month

class TransactionLocalLazyLoader(private val baseDir: File,
                                 private val json: ObjectMapper) {
    private val cache: ObservableMap<Int, ObservableMap<Month, ObservableList<Transaction>>> =
            mutableMapOf<Int, ObservableMap<Month, ObservableList<Transaction>>>().observable().apply {
                addListener(MapChangeListener { change ->
                    if (change.wasAdded()) {
                        val file = File(baseDir, change.key.toString())
                        if (!file.exists()) file.mkdirs()
                    }
                })
            }

    operator fun get(year: Int, month: Month): ObservableList<Transaction> =
            cache.getOrPut(year) { mutableMapOf<Month, ObservableList<Transaction>>().observable() }
                    .getOrPut(month) { loadTransactions(year, month) }
                    .also { println("Access to transactions [$month $year]") }

    private fun loadTransactions(year: Int, month: Month): ObservableList<Transaction> =
            File(baseDir, year.toString()).let { yearDir ->
                if (yearDir.exists()) {
                    File(yearDir, month.ordinal.toString()).let { monthFile ->
                        if (monthFile.exists()) {
                            json.readValue<List<Transaction.DSO>>(monthFile)
                                    .map(Transaction.DSO::toTransaction)
                                    .toMutableList()
                        } else {
                            mutableListOf()
                        }
                    }
                } else {
                    mutableListOf()
                }
            }.observable().apply {
                onChange { change ->
                    json.writeValue(File(File(baseDir, year.toString()), month.ordinal.toString()), change.list.map(Transaction::toDSO))
                }
            }

    operator fun get(id: TransactionID): Transaction? = this[id.year, id.month].firstOrNull { it.id == id }
}
