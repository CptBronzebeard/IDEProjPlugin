import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class RenameIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String {
        return "Rename variable according to current settings"
    }

    var srv: Renamer? = null
    override fun isAvailable(project: Project, p1: Editor?, element: PsiElement): Boolean {
        if (srv == null) {
            //srv = ServiceManager.getService(project, TestService::class.java)
            //srv!!.proj = project
            srv = Renamer.getInstance(project)
        }
        if (element.parent is PsiVariable) {
            val parent = element.parent as PsiVariable
            return !srv!!.checkValidity(parent)
        }
        return false
    }

    override fun invoke(p0: Project, p1: Editor?, p2: PsiElement) {
        srv!!.rename(p2.parent as PsiVariable)
        //srv = ServiceManager.getService(p0, TestService::class.java)
        //RefactoringFactory.getInstance(p0).createRename(p2, srv!!.prefix(p2) + (p2 as PsiVariable).name!!.removePrefix(srv!!.oldPrefix(p2))).run()
    }
}