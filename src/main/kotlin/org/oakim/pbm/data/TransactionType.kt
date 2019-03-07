package org.oakim.pbm.data

import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph

enum class TransactionType(val glyph: FontAwesome.Glyph) {
    PERIOD_START(FontAwesome.Glyph.SIGN_IN), // initial amount @wallet at the beginning of the period
    PERIOD_END(FontAwesome.Glyph.SIGN_OUT), // initial amount @wallet at the closing of the period
    SPENDING(FontAwesome.Glyph.MINUS_SQUARE), // regular transaction type, debit
    INCOME(FontAwesome.Glyph.PLUS_SQUARE), // incoming transaction, credit
    REFUND(FontAwesome.Glyph.UNDO), // refunding transaction
    TRANSFER_IN(FontAwesome.Glyph.LEVEL_DOWN), // incoming transfer transaction
    TRANSFER_OUT(FontAwesome.Glyph.LEVEL_UP); // outgoing transfer transaction

    fun inverse(): TransactionType = when(this) {
        TRANSFER_IN -> TRANSFER_OUT
        TRANSFER_OUT -> TRANSFER_IN
        PERIOD_START -> PERIOD_END
        PERIOD_END -> PERIOD_START
        SPENDING -> REFUND
        REFUND -> SPENDING
        INCOME -> SPENDING
    }

    val displayGlyph
        get() = Glyph("FontAwesome", glyph)
}