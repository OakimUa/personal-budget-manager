package org.oakim.pbm.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.oakim.pbm.service.LocalStorage
import tornadofx.*

class Tag @JsonCreator constructor(
        @JsonProperty("value") value: String = "",
        @JsonProperty("aspect") aspect: String = "",
        @JsonProperty("glyph") glyph: String? = null,
        @JsonProperty("archived") archived: Boolean = false) : NamedGlyph, Archivable {
    @JsonIgnore
    val valueProperty = SimpleStringProperty(this, "value", value)
    var value by valueProperty
    override val nameProperty: StringProperty
        @JsonIgnore
        get() = valueProperty
    override val name: String
        @JsonIgnore
        get() = value

    @JsonIgnore
    val aspectProperty = SimpleStringProperty(this, "aspect", aspect)
    var aspect by aspectProperty

    @JsonIgnore
    override val glyphProperty = SimpleStringProperty(this, "glyph", glyph)
    override var glyph by glyphProperty

    @JsonIgnore
    override val archivedProperty = SimpleBooleanProperty(this, "archived", archived)
    override var archived by archivedProperty

    override fun equals(other: Any?): Boolean =
            (other is Tag? && other?.value == value) ||
                    (other is Tag && other.value == value)

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        fun fromValue(value: String) = LocalStorage.tags.firstOrNull { it.value == value }
    }
}
