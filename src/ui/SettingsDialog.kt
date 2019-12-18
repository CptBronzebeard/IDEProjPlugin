package ui

import Renamer
import Renamer.Style.*
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.buttonGroup
import com.intellij.ui.layout.panel
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JTextField

class SettingsDialog(val ts: Renamer) : DialogWrapper(true) {
    private var lp: String = ""
    override fun createCenterPanel(): JComponent? {
        val pan = panel {
            row("Local variables prefix") { textField(ts::localPrefix).errorOnInvalid() }
            row("Constants prefix") { textField(ts::constPrefix).errorOnInvalid() }
            row("Other fields prefix", true) { textField(ts::fieldPrefix).errorOnInvalid() }
            row("Word separation style: ") {
                buttonGroup(ts::style) {
                    row { radioButton("camelCase", CAMELCASE) }
                    row { radioButton("snake_case", SNAKECASE) }
                    row { radioButton("UPPER_SNAKE_CASE", UPPERSNAKECASE) }
                }
            }

        }
        pan.minimumSize = Dimension(600, 300)
        return pan
    }

    private fun CellBuilder<JTextField>.errorOnInvalid(): CellBuilder<JTextField> {
        return this.withErrorOnApplyIf("Invalid prefix") { it.text.contains("[\\W]".toRegex()) }
    }

    init {
        init()
        title = "Codestyle settings"
    }
}