package actions

import com.intellij.codeInsight.completion.AllClassesGetter
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.search.GlobalSearchScope
import processors.GlobalRenamingProcessor

class GlobalRenamingAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        if (project != null) {
            val processor = GlobalRenamingProcessor()
            //batchGroupBy.start()
            AllClassesGetter.processJavaClasses(
                    PlainPrefixMatcher(""),
                    project,
                    GlobalSearchScope.projectScope(project),
                    processor
            )
        }
    }

}