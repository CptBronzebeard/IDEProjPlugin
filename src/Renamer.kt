import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiUtil
import com.intellij.refactoring.RefactoringFactory

class Renamer(val proj: Project) {
    val factory = RefactoringFactory.getInstance(proj)
    val localPref: PropertiesComponent = PropertiesComponent.getInstance(proj)
    var prevLocal: String
        get() = localPref.getValue("LOCAL_VAR_CONST_PREFIX", "")
        set(value) {
            localPref.setValue("LOCAL_VAR_CONST_PREFIX", value)
        }
    var localPrefix: String
        get() = localPref.getValue("LOCAL_VAR_PREFIX", "")
        set(value) {
            prevLocal = localPref.getValue("LOCAL_VAR_PREFIX", "")
            localPref.setValue("LOCAL_VAR_PREFIX", value)
        }
    var prevConst: String
        get() = localPref.getValue("LOCAL_PREV_CONST_PREFIX", "")
        set(value) {
            localPref.setValue("LOCAL_PREV_CONST_PREFIX", value)
        }
    var constPrefix: String
        get() = localPref.getValue("LOCAL_CONST_PREFIX", "")
        set(value) {
            prevConst = localPref.getValue("LOCAL_CONST_PREFIX", "")
            localPref.setValue("LOCAL_CONST_PREFIX", value)
        }

    fun prefix(element: PsiElement): String {
        val test = prefixes[element.javaClass.interfaces[0]]
        return test!!()
    }

    fun oldPrefix(element: PsiElement): String {
        val test = oldPrefixes[element.javaClass.interfaces[0]]
        return test!!()
    }

    var prefixes = hashMapOf<Class<out PsiElement>, () -> String>(PsiLocalVariable::class.java to { localPref.getValue("LOCAL_VAR_PREFIX", "") }, PsiField::class.java to { localPref.getValue("LOCAL_CONST_PREFIX", "") })
    var oldPrefixes = hashMapOf<Class<out PsiElement>, () -> String>(PsiLocalVariable::class.java to { prevLocal }, PsiField::class.java to { prevConst })
    fun checkValidity(element: PsiVariable): Boolean {
        return if (element is PsiLocalVariable || PsiUtil.isCompileTimeConstant(element))
            element.name!!.startsWith(prefix(element))
        else true
    }

    fun rename(element: PsiElement) {
        val list = ReferencesSearch.search(element).toList()
        val toProcess =
                if (element is PsiReferenceExpression)
                    (element as PsiJavaCodeReferenceElement).resolve() as PsiVariable
                else element as PsiVariable
        val el = prefix(toProcess) + toProcess.name!!.removePrefix(oldPrefix(toProcess))
        toProcess.setName(el)
        list.forEach { it.handleElementRename(el) }
    }

    companion object {
        private var instance: Renamer? = null
        fun getInstance(proj: Project): Renamer {
            if (instance == null) {
                instance = Renamer(proj)
                return instance!!
            }
            return instance!!
        }
    }
}