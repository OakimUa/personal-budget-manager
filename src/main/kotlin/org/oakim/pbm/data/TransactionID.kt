package org.oakim.pbm.data

import org.oakim.pbm.service.LocalStorage
import java.time.LocalDate
import java.time.Month
import java.util.*

data class TransactionID(val year: Int, val month: Month, val uid: String) {
    fun toRef() = "$year:${month.value}:$uid"
    fun transaction(): Transaction = LocalStorage.transactions[year, month].firstOrNull { it.id == this }
            ?: throw Exception("Broken reference: $this")

    companion object {
        fun new() = LocalDate.now().let { new(it.year, it.month) }
        fun new(year: Int, month: Month): TransactionID = TransactionID(year, month, UUID.randomUUID().toString())

        fun fromRef(ref: String): TransactionID = ref.split(":").let {
            TransactionID(it[0].toInt(), Month.of(it[1].toInt()), it[2])
        }
    }
}