import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class RenameIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String {
        return "Prefix variable"
    }

    override fun getText(): String = "Normalize elements' name"
    var srv: Renamer? = null
    override fun isAvailable(project: Project, p1: Editor?, element: PsiElement): Boolean {
        if (srv == null) {
            //srv = ServiceManager.getService(project, TestService::class.java)
            //srv!!.proj = project
            srv = Renamer.getInstance(project)
        }
        if (element.parent is PsiReferenceExpression) {
            val parent = (element.parent!! as PsiJavaCodeReferenceElement).resolve()
            if (parent is PsiVariable)
                return !srv!!.checkValidity(parent)
        }
        if (element.parent is PsiVariable) {
            val parent = element.parent as PsiVariable
            return !srv!!.checkValidity(parent)
        }
        return false
    }

    override fun invoke(p0: Project, p1: Editor?, p2: PsiElement) {
        srv!!.rename(p2.parent)
    }
}