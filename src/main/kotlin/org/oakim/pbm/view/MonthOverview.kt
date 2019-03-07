package org.oakim.pbm.view

import javafx.scene.control.ToggleButton
import org.controlsfx.control.SegmentedButton
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.oakim.pbm.app.Selector
import org.oakim.pbm.app.Styles
import tornadofx.*
import tornadofx.controlsfx.segmentedbutton
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class MonthOverview : View("Month Overview", Glyph("FontAwesome", FontAwesome.Glyph.CALENDAR)), ScopedInstance {
    override val root = vbox(spacing = 10) {
        addClass(Styles.content)
        hbox(10) {
            combobox(Selector.selectedYear, Selector.yearList)
            segmentedbutton {
                addClass(SegmentedButton.STYLE_CLASS_DARK)
                buttons.addAll(Month.values().map { month ->
                    ToggleButton(month.getDisplayName(TextStyle.FULL, Locale.getDefault())).apply {
                        isSelected = Selector.selectedMonth.value == month
                        action {
                            Selector.selectedMonth.value = month
                        }
                    }
                })
            }
        }
        tabpane {
            tab(TransactionDetailedOverview::class)
            tab(TransactionInputOverview::class)
            tab("Summary") {
                isClosable = false
                label("TBD")
            }
            tab("Diagram") {
                isClosable = false
                label("TBD")
            }
        }
    }
}
