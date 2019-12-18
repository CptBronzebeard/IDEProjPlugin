package ui
import Renamer
import Renamer.Style.*
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.buttonGroup
import com.intellij.ui.layout.panel
import java.awt.Dimension
import javax.swing.JComponent

class SettingsDialog(val ts: Renamer) : DialogWrapper(true) {
    override fun createCenterPanel(): JComponent? {
        val pan = panel {
            row("Local variables prefix") { textField(ts::localPrefix) }
            row("Constants prefix") { textField(ts::constPrefix) }
            row("Other fields prefix", true) { textField(ts::fieldPrefix) }
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

    init {
        init()
        title = "Codestyle settings"
    }
}