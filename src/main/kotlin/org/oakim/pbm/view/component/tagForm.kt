package org.oakim.pbm.view.component

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventTarget
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Tag
import org.oakim.pbm.model.TagModel
import org.oakim.pbm.service.LocalStorage
import tornadofx.*
import java.util.concurrent.Callable

fun EventTarget.tagForm(model: TagModel,
                        isNewTag: SimpleBooleanProperty,
                        commitNew: (Tag) -> Unit,
                        commitEdit: (Tag) -> Unit): Form = form {
    addClass(Styles.content)
    fieldset {
        hbox(10) {
            field("Glyph") {
                combobox(model.glyph, FontAwesome.Glyph.values().map { it.name }.observable()) {
                    cellFormat(FX.defaultScope) {
                        graphic = label(item, graphic = Glyph("FontAwesome", FontAwesome.Glyph.valueOf(item)))
                    }
                }
            }
            field("Tag value") {
                textfield(model.value) {
                    disableProperty().bind(isNewTag.not())
                }
            }
            field("Tag aspect") {
                popInfo("Aspect defines a nature of the tag: 'Wallet' for tagging wallets, or 'Country' for tagging expenses for holidays.")
                textfield(model.aspect)
            }
            button("Add/Update") {
                textProperty().bind(Bindings.createStringBinding(Callable { if (isNewTag.value) "Add" else "Update" }, isNewTag))
            }.action {
                model.commit()
                if (isNewTag.value) {
                    commitNew(model.item)
                    model.item = Tag()
                } else {
                    commitEdit(model.item)
                }
                LocalStorage.storeTags()
            }
            button("Reset").action {
                model.rollback()
            }
            button("New").action {
                model.rollback()
                model.item = Tag()
                isNewTag.value = true
            }
        }
    }
}

fun EventTarget.walletTagForm(model: TagModel,
                              isNewTag: SimpleBooleanProperty,
                              commitNew: (Tag) -> Unit,
                              commitEdit: (Tag) -> Unit): Form = form {
    addClass(Styles.content)
    fieldset {
        field("Glyph") {
            combobox(model.glyph, FontAwesome.Glyph.values().map { it.name }.observable()) {
                cellFormat(FX.defaultScope) {
                    graphic = label(item, graphic = Glyph("FontAwesome", FontAwesome.Glyph.valueOf(item)))
                }
            }
        }
        field("Tag value") {
            textfield(model.value) {
                disableProperty().bind(isNewTag.not())
            }
        }
        field("Tag aspect") {
            textfield(model.aspect)
        }
        field(forceLabelIndent = true) {
            hbox(10) {
                button("Add/Update") {
                    textProperty().bind(Bindings.createStringBinding(Callable { if (isNewTag.value) "Add" else "Update" }, isNewTag))
                }.action {
                    model.commit()
                    if (isNewTag.value) {
                        commitNew(model.item)
                        model.item = Tag()
                    } else {
                        commitEdit(model.item)
                    }
                    LocalStorage.storeWalletTags()
                }
                button("Reset").action {
                    model.rollback()
                }
                button("New").action {
                    model.rollback()
                    model.item = Tag()
                    isNewTag.value = true
                }
            }
        }
    }
}