import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class RenameIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String {
        return "Prefix variable"
    }

    override fun getText(): String = "Normalize elements' name"
    var srv: Renamer? = null
    override fun isAvailable(project: Project, p1: Editor?, element: PsiElement): Boolean {
        if (srv == null) {
            srv = Renamer.getInstance(project)
        }
        return !srv!!.checkValidity(element.parent)
    }

    override fun invoke(p0: Project, p1: Editor?, p2: PsiElement) {
        srv!!.addPrefix(p2.parent)
    }
}