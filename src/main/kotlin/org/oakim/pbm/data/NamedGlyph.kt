package org.oakim.pbm.data

import com.fasterxml.jackson.annotation.JsonIgnore
import javafx.beans.property.StringProperty
import javafx.scene.control.Label
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph

interface NamedGlyph {
    val nameProperty: StringProperty
    val name: String
    val glyphProperty: StringProperty
    val glyph: String?

    val displayGlyph: Glyph @JsonIgnore get() = Glyph("FontAwesome", if (glyph == null) FontAwesome.Glyph.BAN else FontAwesome.Glyph.valueOf(glyph!!))
    val displayNode: Label @JsonIgnore get() = Label(name, displayGlyph)
}
