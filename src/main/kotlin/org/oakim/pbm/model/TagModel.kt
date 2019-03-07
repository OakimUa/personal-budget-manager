package org.oakim.pbm.model

import org.oakim.pbm.data.Tag
import tornadofx.*

class TagModel(tag: Tag) : ItemViewModel<Tag>(tag) {
    val value = bind(Tag::valueProperty)
    val aspect = bind(Tag::aspect)
    val glyph = bind(Tag::glyph)
}