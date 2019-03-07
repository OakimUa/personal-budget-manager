package org.oakim.pbm.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.TableView
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Styles
import org.oakim.pbm.data.Tag
import org.oakim.pbm.data.Wallet
import org.oakim.pbm.model.TagModel
import org.oakim.pbm.model.WalletModel
import org.oakim.pbm.service.LocalStorage
import org.oakim.pbm.view.component.segmentedPBMEntityMultiSelector
import org.oakim.pbm.view.component.walletForm
import org.oakim.pbm.view.component.walletTagForm
import tornadofx.*
import tornadofx.controlsfx.toggleswitch

class WalletOverview : View("Wallets", Glyph("FontAwesome", FontAwesome.Glyph.MONEY)), ScopedInstance {
    private var walletTableView: TableView<Wallet> by singleAssign()
    private var tagTableView: TableView<Tag> by singleAssign()

    private val walletModel = WalletModel(Wallet())
    private val tagModel = TagModel(Tag())

    private val filterTags = mutableListOf<Tag>().observable().apply {
        addAll(LocalStorage.walletTags)
        onChange { wallets.refilter() }
        onChange { println("Selected tags: ${it.list.map { it.value }}") }
    }

    private val showActiveWallets = SimpleBooleanProperty(true)
    private val showArchivedWallets = SimpleBooleanProperty(false)

    private val showActiveTags = SimpleBooleanProperty(true)
    private val showArchivedTags = SimpleBooleanProperty(false)

    private val wallets: SortedFilteredList<Wallet> by lazy {
        SortedFilteredList(LocalStorage.wallets).apply {
            predicate = { wallet ->
                (wallet.tags.isEmpty() || wallet.tags.any { tag -> tag in filterTags }) &&
                        ((!wallet.archived && showActiveWallets.value) || (wallet.archived && showArchivedWallets.value))
            }
        }
    }

    private val tags: SortedFilteredList<Tag> by lazy {
        SortedFilteredList(LocalStorage.walletTags).apply {
            predicate = { tag -> (!tag.archived && showActiveTags.value) || (tag.archived && showArchivedTags.value) }
        }
    }

    private val isNewWallet = SimpleBooleanProperty(true)
    private val isNewTag = SimpleBooleanProperty(true)

    override val root = vbox {
        useMaxWidth = true
        addClass(Styles.content)

        borderpane {
            useMaxWidth = true
            top = hbox(10) {
                segmentedPBMEntityMultiSelector(
                        selected = filterTags,
                        values = LocalStorage.walletTags)
                toggleswitch("Active", showActiveWallets).selectedProperty().onChange { wallets.refilter() }
                toggleswitch("Archived", showArchivedWallets).selectedProperty().onChange { wallets.refilter() }
            }
            center = tableview(wallets) {
                walletTableView = this
                readonlyColumn("Name", Wallet::name).cellFormat { graphic = rowItem.displayNode }
                readonlyColumn("Tags", Wallet::tags).cellFormat { tags -> graphic = hbox(10) { tags.forEach { this += it.displayNode } } }
                readonlyColumn("Notes", Wallet::notes).remainingWidth()
                readonlyColumn("", Wallet::archived).cellFormat { graphic = rowItem.stateNode }
                readonlyColumn("", Wallet::archived).cellFormat {
                    graphic = rowItem.stateManagementNode {
                        refresh()
                        wallets.refilter()
                        LocalStorage.storeWallets()
                    }
                }
                wallets.onChange { requestResize() }
                bindSelected(walletModel)
                onSelectionChange { isNewWallet.value = false }
                requestResize()
            }
            right = walletForm(model = walletModel, isNewWallet = isNewWallet,
                    commitNew = { wallet ->
                        if (wallets.none { it.name == wallet.name })
                            wallets.add(wallet)
                        else
                            throw Exception("Wallet with name '${wallet.name}' already exists") // todo: notification
                    },
                    commitEdit = {
                        walletTableView.refresh()
                    })
        }

        borderpane {
            top = hbox(spacing = 10) {
                addClass(Styles.content)
                toggleswitch("Active", showActiveTags).selectedProperty().onChange { tags.refilter() }
                toggleswitch("Archived", showArchivedTags).selectedProperty().onChange { tags.refilter() }
            }
            left = tableview(tags) {
                tagTableView = this
                prefWidthProperty().bind(walletTableView.widthProperty())
                maxWidthProperty().bind(walletTableView.widthProperty())
                alignment = Pos.TOP_LEFT
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
                bindSelected(tagModel)
                onSelectionChange { isNewTag.value = false }
            }
            right = walletTagForm(model = tagModel, isNewTag = isNewTag,
                    commitNew = { tag ->
                        if (tags.none { it.value == tag.value })
                            tags.add(tag)
                        else
                            throw Exception("Tag with value '${tag.value}' already exists") // todo: notification
                    },
                    commitEdit = {
                        tagTableView.refresh()
                    })
        }
    }
}
