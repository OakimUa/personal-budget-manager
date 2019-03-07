package org.oakim.pbm.app

import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val heading by cssclass()
        val content by cssclass()
        val row by cssclass()
        val labelInRow by cssclass()
    }

    init {
        label and heading {
            padding = box(10.px)
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
        }
        content {
            padding = box(10.px)
        }
        labelInRow {
            padding = box(0.px, 0.px, 0.px, 10.px)
        }
        row {
            spacing = 10.px
        }
    }
}