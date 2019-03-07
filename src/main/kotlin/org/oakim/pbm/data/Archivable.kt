package org.oakim.pbm.data

import com.fasterxml.jackson.annotation.JsonIgnore
import javafx.beans.property.BooleanProperty
import javafx.scene.control.Hyperlink
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph

interface Archivable {
    val archivedProperty: BooleanProperty
    var archived: Boolean

    val stateNode: Glyph @JsonIgnore get() = Glyph("FontAwesome", if (archived) FontAwesome.Glyph.RECYCLE else FontAwesome.Glyph.FLASH)
    fun stateManagementNode(postAction: (Boolean) -> Unit = {}) =
            if (!archived)
                Hyperlink("delete").apply {
                    setOnAction {
                        archived = true
                        postAction(true)
                    }
                }
            else
                Hyperlink("restore").apply {
                    setOnAction {
                        archived = false
                        postAction(false)
                    }
                }
}
