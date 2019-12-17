import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

class RestyleIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String {
        return "Restyle variable"
    }

    override fun getText(): String = "Restyle elements name"
    var srv: Renamer? = null
    override fun isAvailable(project: Project, p1: Editor?, element: PsiElement): Boolean {
        if (srv == null) {
            srv = Renamer.getInstance(project)
        }
        return !srv!!.isStyled(element.parent)
    }

    override fun invoke(p0: Project, p1: Editor?, p2: PsiElement) {
        srv!!.changeStyle(p2.parent as PsiNamedElement)
    }
}