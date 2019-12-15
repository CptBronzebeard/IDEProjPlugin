import com.intellij.codeInsight.completion.AllClassesGetter
import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.RefactoringFactory
import com.intellij.util.Processor
import processors.GlobalRenamingProcessor


class JavaClassPrefixer : AnAction("Prefix Java classes") {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PlatformDataKeys.PROJECT)
        var namings = HashMap<PsiElement, String>()
        val processor = GlobalRenamingProcessor()

        AllClassesGetter.processJavaClasses(
                PlainPrefixMatcher(""),
                project!!,
                GlobalSearchScope.projectScope(project),
                processor
        )
    }

}

