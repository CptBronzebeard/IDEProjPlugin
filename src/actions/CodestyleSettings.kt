package actions

import Renamer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import ui.SettingsDialog


class CodestyleSettings : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        val ts = Renamer.getInstance(project!!)
        SettingsDialog(ts).showAndGet()
    }

}

