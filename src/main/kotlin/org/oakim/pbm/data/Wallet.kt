package org.oakim.pbm.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableSet
import org.oakim.pbm.service.LocalStorage
import tornadofx.*

class Wallet(name: String = "",
             tags: Set<Tag> = setOf(),
             glyph: String? = null,
             notes: String? = null,
             archived: Boolean = false) : NamedGlyph, Archivable {
    override val nameProperty = SimpleStringProperty(this, "name", name)
    override var name: String by nameProperty

    val tagsProperty = SimpleSetProperty<Tag>(this, "tags", tags.observable())
    var tags: ObservableSet<Tag> by tagsProperty

    override val glyphProperty = SimpleStringProperty(this, "glyph", glyph)
    override var glyph: String by glyphProperty

    val notesProperty = SimpleStringProperty(this, "notes", notes)
    var notes: String by notesProperty

    override val archivedProperty = SimpleBooleanProperty(this, "archived", archived)
    override var archived: Boolean by archivedProperty

    data class DSO @JsonCreator constructor(
            @JsonProperty("name") val name: String,
            @JsonProperty("tags") val tags: Set<String>,
            @JsonProperty("glyph") val glyph: String?,
            @JsonProperty("notes") val notes: String?,
            @JsonProperty("archived") val archived: Boolean) {
        fun toWallet() = Wallet(name, LocalStorage.walletTags.filter { it.value in tags }.toSet(), glyph, notes, archived)
    }

    fun toDSO() = DSO(name, tags.map(Tag::value).toSet(), glyph, notes, archived)

    companion object {
        fun fromName(name: String?) = if (name == null) null else LocalStorage.wallets.firstOrNull { it.name == name }
    }
}