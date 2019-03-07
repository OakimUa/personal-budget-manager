package org.oakim.pbm.model

import org.oakim.pbm.data.Wallet
import tornadofx.*

class WalletModel(wallet: Wallet) : ItemViewModel<Wallet>(wallet) {
    val name = bind(Wallet::nameProperty)
    val tags = bind(Wallet::tagsProperty)
    val glyph = bind(Wallet::glyphProperty)
    val notes = bind(Wallet::notesProperty)
}