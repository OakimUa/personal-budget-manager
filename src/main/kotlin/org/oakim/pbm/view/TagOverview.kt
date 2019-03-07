package org.oakim.pbm.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.TableView
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Tag
import org.oakim.pbm.model.TagModel
import org.oakim.pbm.service.LocalStorage
import org.oakim.pbm.view.component.tagForm
import tornadofx.*
import tornadofx.controlsfx.toggleswitch

class TagOverview : View("Tags", Glyph("FontAwesome", FontAwesome.Glyph.TAGS)), ScopedInstance {
    private var tableView: TableView<Tag> by singleAssign()
    private val model = TagModel(Tag())

    private val showActive = SimpleBooleanProperty(true)
    private val showArchived = SimpleBooleanProperty(false)

    private val tags: SortedFilteredList<Tag> by lazy {
        SortedFilteredList(LocalStorage.tags).apply {
            predicate = { tag -> (!tag.archived && showActive.value) || (tag.archived && showArchived.value) }
        }
    }

    private val isNewTag = SimpleBooleanProperty(true)

    override val root = borderpane {
        top = hbox(spacing = 10) {
            addClass(Styles.content)
            toggleswitch("Active", showActive).selectedProperty().onChange { tags.refilter() }
            toggleswitch("Archived", showArchived).selectedProperty().onChange { tags.refilter() }
        }
        center = vbox {
            addClass(Styles.content)
            tableView = tableview(tags) {
                readonlyColumn("Value", Tag::value).cellFormat { graphic = rowItem.displayNode }
                readonlyColumn("Tag Aspect", Tag::aspect).remainingWidth()
                readonlyColumn("", Tag::archived).cellFormat { graphic = rowItem.stateNode }
                readonlyColumn("", Tag::archived).cellFormat {
                    graphic = rowItem.stateManagementNode {
                        refresh()
                        tags.refilter()
                        LocalStorage.storeTags()
                    }
                }
                requestResize()
                tags.onChange { requestResize() }
                bindSelected(model)
                onSelectionChange { isNewTag.value = false }
            }
        }
        bottom = tagForm(model = model, isNewTag = isNewTag,
                commitNew = { tag ->
                    if (tags.none { it.value == tag.value })
                        tags.add(tag)
                    else
                        throw Exception("Tag with value '${tag.value}' already exists") // todo: notification
                },
                commitEdit = {
                    tableView.refresh()
                })
    }
}
