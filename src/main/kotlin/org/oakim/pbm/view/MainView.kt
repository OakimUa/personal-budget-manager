package org.oakim.pbm.view

import org.oakim.pbm.app.Selector
import org.oakim.pbm.app.Styles
import tornadofx.*

class MainView : View("Personal budget manager") {
    private val monthOverview: MonthOverview by inject()
    private val walletOverview: WalletOverview by inject()
    private val tagOverview: TagOverview by inject()
    private val parserOverview: ParserOverview by inject()

    override val root = vbox {
        label(title) {
            addClass(Styles.heading)
            val stateText = Selector.hasChanges.stringBinding { wasChanges ->
                if (wasChanges == true) "$title [*]" else title
            }
            textProperty().bind(stateText)
        }
        drawer {
            item(monthOverview)
            item(walletOverview)
            item(tagOverview)
            item(parserOverview, expanded = true)
        }
    }

    override fun onDock() {
        super.onDock()
        primaryStage.isMaximized = true
    }
}