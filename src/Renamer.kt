import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.PsiUtil.isCompileTimeConstant

class Renamer(val proj: Project) {
    private val localPref: PropertiesComponent = PropertiesComponent.getInstance(proj)
    private var prefixes =
            hashMapOf<Class<out PsiElement>, () -> String>(PsiLocalVariable::class.java to { localPref.getValue("LOCAL_VAR_PREFIX", "") }, PsiField::class.java to { localPref.getValue("LOCAL_CONST_PREFIX", "") })
    private var oldPrefixes = hashMapOf<Class<out PsiElement>, () -> String>(PsiLocalVariable::class.java to { prevLocal }, PsiField::class.java to { prevConst })
    private val styles =
            hashMapOf<String, Style>("camelCase" to Style.CAMELCASE, "snake_case" to Style.SNAKECASE, "UPPER_SNAKE_CASE" to Style.UPPERSNAKECASE)
    private val matchers =
            hashMapOf<Style, (PsiNamedElement) -> Boolean>(Style.CAMELCASE to ::isCamel, Style.SNAKECASE to ::isSnake, Style.UPPERSNAKECASE to ::isUpperSnake)
    private val renamers =
            hashMapOf<Style, (PsiNamedElement) -> String>(Style.CAMELCASE to ::toCamel, Style.SNAKECASE to ::toSnake, Style.UPPERSNAKECASE to ::toUpperSnake)
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

    enum class Style(val code: String) {
        CAMELCASE("camelCase"),
        SNAKECASE("snake_case"),
        UPPERSNAKECASE("UPPER_SNAKE_CASE")
    }

    var style: Style
        get() = styles[localPref.getValue("CODESTYLE_TYPE", "camelCase")]!!
        set(value) {
            localPref.setValue("CODESTYLE_TYPE", value.code)
        }

    private fun prefix(element: PsiElement): String {
        if (element is PsiVariable) {
            if (element is PsiLocalVariable || isCompileTimeConstant(element)) {
                val test = prefixes[element.javaClass.interfaces[0]]
                return test!!()
            }
        }
        return ""
    }

    private fun oldPrefix(element: PsiElement): String {
        val test = oldPrefixes[element.javaClass.interfaces[0]]
        return test!!()
    }


    fun checkValidity(element: PsiElement): Boolean {
        var toCheck =
                when (element) {
                    is PsiReferenceExpression -> {
                        (element as PsiJavaCodeReferenceElement).resolve()
                    }
                    else -> element
                }
        if (element is PsiVariable)
            if (element is PsiLocalVariable || PsiUtil.isCompileTimeConstant(element))
                return element.name!!.startsWith(prefix(element))
        return true
    }

    fun addPrefix(element: PsiElement) {

        val toProcess =
                if (element is PsiReferenceExpression)
                    (element as PsiJavaCodeReferenceElement).resolve() as PsiVariable
                else element as PsiVariable
        val el = prefix(toProcess) + toProcess.name!!.removePrefix(oldPrefix(toProcess))
        renameEl(toProcess, el)
    }

    fun isStyled(element: PsiElement): Boolean {
        return if (element is PsiNamedElement)
            matchers[style]!!(element)
        else true
    }

    fun changeStyle(element: PsiNamedElement) {
        renameEl(element, renamers[style]!!(element))
    }

    private fun toCamel(element: PsiNamedElement): String {
        val prefix = getPref(element)
        val name = element.name!!.removePrefix(prefix)
        return prefix + if (name.contains(Regex("[a-z]")))
            name.replace("_+[a-zA-Z]".toRegex()) { it -> it.value.replace("_", "").capitalize() } else
            name.toLowerCase().replace("_+[a-z]".toRegex()) {it.value.replace("_", "").capitalize()}
    }

    private fun toSnake(element: PsiNamedElement): String {
        val prefix = getPref(element)
        val name = element.name!!.removePrefix(prefix)
        return prefix + if (name.contains(Regex("[a-z]")))
            name.replace('-', '_').replace("[A-Z]".toRegex()) { it -> "_" + it.value.toLowerCase() } else
            name.toLowerCase()
    }

    private fun toUpperSnake(element: PsiNamedElement): String {
        val prefix = getPref(element)
        return prefix + (element.name!!.removePrefix(prefix).replace('-', '_').replace("[A-Z]".toRegex()) { it -> "_" + it.value.toLowerCase() }).toUpperCase()
    }

    private fun isCamel(element: PsiNamedElement): Boolean {
        val name = element.name!!.removePrefix(prefix(element))
        return !(name.contains('_') || name.contains('-'))
    }

    private fun isSnake(element: PsiNamedElement): Boolean {
        val name = element.name!!.removePrefix(prefix(element))
        return !(name.contains(Regex("[A-Z]")) || name.contains('-'))
    }

    private fun isUpperSnake(element: PsiNamedElement): Boolean {
        val name = element.name!!.removePrefix(prefix(element))
        return !(name.contains(Regex("[a-z]")) || name.contains('-'))
    }

    private fun renameEl(element: PsiNamedElement, newName: String) {
        val list = ReferencesSearch.search(element).toList()
        element.setName(newName)
        list.forEach { it.handleElementRename(newName) }
    }

    private fun getPref(element: PsiNamedElement): String {
        return if (element.name!!.startsWith(prefix(element)))
            prefix(element)
        else ""
    }

    fun getRenamer(): ((PsiNamedElement) -> String)? {
        return renamers[style]
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