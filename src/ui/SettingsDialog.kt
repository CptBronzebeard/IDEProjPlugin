package ui

import Renamer
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import java.awt.Dimension
import javax.swing.JComponent


class SettingsDialog(val ts: Renamer) : DialogWrapper(true) {
    override fun createCenterPanel(): JComponent? {
        val pan = panel {
            row("Local variables prefix") { textField(ts::localPrefix) }
            row("Constants prefix") { textField(ts::constPrefix) }
        }
        pan.minimumSize = Dimension(1000, 1000)
        return pan
    }

    init {
        init()
        title = "Codestyle settings"
    }
}