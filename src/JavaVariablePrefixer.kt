import com.intellij.codeInsight.completion.AllClassesGetter
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.search.GlobalSearchScope
import processors.GlobalRenamingProcessor
import ui.SettingsDialog


class JavaVariablePrefixer : AnAction("Prefix Java classes") {
    override fun actionPerformed(event: AnActionEvent) {
        //println((TestService::localPrefix).returnType)
        val project = event.getData(PlatformDataKeys.PROJECT)
        val ts = TestService(project!!)
        //SettingsDialog(ts).showAndGet()

        val processor = GlobalRenamingProcessor()
        AllClassesGetter.processJavaClasses(
                PlainPrefixMatcher(""),
                project,
                GlobalSearchScope.projectScope(project),
                processor
        )
    }

}

