import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class RenameIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String {
        return "Prefix variable"
    }

    var msg: String = "Add prefix"

    override fun getText(): String = msg
    var srv: Renamer? = null
    override fun isAvailable(project: Project, p1: Editor?, element: PsiElement): Boolean {
        if (srv == null) {
            srv = Renamer.getInstance(project)
        }
        msg = "Add \"" + srv!!.prefix(element.parent) + "\" prefix"
        return !srv!!.checkValidity(element.parent)
    }

    override fun invoke(p0: Project, p1: Editor?, p2: PsiElement) {
        srv!!.addPrefix(p2.parent)
    }
}