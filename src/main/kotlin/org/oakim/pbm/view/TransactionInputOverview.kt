package org.oakim.pbm.view

import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.data.TransactionType
import org.oakim.pbm.view.component.simpleRefundForm
import org.oakim.pbm.view.component.simpleTransactionForm
import org.oakim.pbm.view.component.simpleTransferForm
import tornadofx.*

class TransactionInputOverview : View("Data Input", Glyph("FontAwesome", FontAwesome.Glyph.PENCIL_SQUARE)) {
    override val root = drawer {
        item(title = "Траты", icon = Glyph("FontAwesome", FontAwesome.Glyph.MINUS_SQUARE)) {
            simpleTransactionForm(TransactionType.SPENDING)
        }
        item(title = "Доход", icon = Glyph("FontAwesome", FontAwesome.Glyph.PLUS_SQUARE)) {
            simpleTransactionForm(TransactionType.INCOME)
        }
        item(title = "Возврат", icon = Glyph("FontAwesome", FontAwesome.Glyph.UNDO)) {
            simpleRefundForm()
        }
        item(title = "Трансфер", icon = Glyph("FontAwesome", FontAwesome.Glyph.RETWEET)) {
            simpleTransferForm()
        }
        item(title = "Загрузка выписки", icon = Glyph("FontAwesome", FontAwesome.Glyph.UPLOAD)) {
            label("TBD")
        }
    }

    init {
        this.disableClose()
    }
}
