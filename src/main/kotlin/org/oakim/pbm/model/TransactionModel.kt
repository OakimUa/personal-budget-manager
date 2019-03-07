package org.oakim.pbm.model

import org.oakim.pbm.data.Transaction
import tornadofx.*

class TransactionModel(transaction: Transaction) : ItemViewModel<Transaction>(transaction) {
    val amount = bind(Transaction::amount)
    val type = bind(Transaction::type)
    val tags = bind(Transaction::tags)
    val wallet = bind(Transaction::wallet)
    val reference = bind(Transaction::reference)
    val notes = bind(Transaction::notes)
}