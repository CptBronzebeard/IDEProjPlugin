import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiUtil.isCompileTimeConstant

class Renamer(val proj: Project) {
    private val localPref: PropertiesComponent = PropertiesComponent.getInstance(proj)
    private var prefixes =
            hashMapOf<Type, () -> String>(Type.LOCAL to { localPref.getValue(LOCAL_VAR_PREFIX, "") },
                    Type.CONST to { localPref.getValue(LOCAL_CONST_PREFIX, "") },
                    Type.FIELD to { localPref.getValue(LOCAL_FIELD_PREFIX, "") })
    private var oldPrefixes = hashMapOf<Type, () -> String>(Type.LOCAL to { prevLocal }, Type.CONST to { prevConst }, Type.FIELD to { prevField })
    private val styles =
            hashMapOf<String, Style>("camelCase" to Style.CAMELCASE, "snake_case" to Style.SNAKECASE, "UPPER_SNAKE_CASE" to Style.UPPERSNAKECASE)
    private val matchers =
            hashMapOf<Style, (PsiNamedElement) -> Boolean>(Style.CAMELCASE to ::isCamel, Style.SNAKECASE to ::isSnake, Style.UPPERSNAKECASE to ::isUpperSnake)
    private val renamers =
            hashMapOf<Style, (PsiNamedElement) -> String>(Style.CAMELCASE to ::toCamel, Style.SNAKECASE to ::toSnake, Style.UPPERSNAKECASE to ::toUpperSnake)
    var prevLocal: String
        get() = localPref.getValue(LOCAL_PREV_VAR_PREFIX, "")
        set(value) {
            localPref.setValue(LOCAL_PREV_VAR_PREFIX, value)
        }
    var localPrefix: String
        get() = localPref.getValue(LOCAL_VAR_PREFIX, "")
        set(value) {
            prevLocal = localPref.getValue(LOCAL_VAR_PREFIX, "")
            localPref.setValue(LOCAL_VAR_PREFIX, value)
        }
    var prevConst: String
        get() = localPref.getValue(LOCAL_PREV_CONST_PREFIX, "")
        set(value) {
            localPref.setValue(LOCAL_PREV_CONST_PREFIX, value)
        }
    var constPrefix: String
        get() = localPref.getValue(LOCAL_CONST_PREFIX, "")
        set(value) {
            prevConst = localPref.getValue(LOCAL_CONST_PREFIX, "")
            localPref.setValue(LOCAL_CONST_PREFIX, value)
        }
    var prevField: String
        get() = localPref.getValue(LOCAL_PREV_FIELD_PREFIX, "")
        set(value) {
            localPref.setValue(LOCAL_PREV_FIELD_PREFIX, value)
        }
    var fieldPrefix: String
        get() = localPref.getValue(LOCAL_FIELD_PREFIX, "")
        set(value) {
            prevField = localPref.getValue(LOCAL_FIELD_PREFIX, "")
            localPref.setValue(LOCAL_FIELD_PREFIX, value)
        }

    enum class Style(val code: String) {
        CAMELCASE("camelCase"),
        SNAKECASE("snake_case"),
        UPPERSNAKECASE("UPPER_SNAKE_CASE")
    }

    enum class Type(val code: String) {
        LOCAL("Local"),
        CONST("Constant"),
        FIELD("Field")
    }

    var style: Style
        get() = styles[localPref.getValue("CODESTYLE_TYPE", "camelCase")]!!
        set(value) {
            localPref.setValue("CODESTYLE_TYPE", value.code)
        }

    fun prefix(element: PsiElement): String {
        if (element is PsiVariable) {
            if (element is PsiLocalVariable || element is PsiField) {
                val test = prefixes[variableType(element)]
                return test!!()
            }
        }
        return ""
    }

    private fun variableType(element: PsiVariable): Type {
        return if (element is PsiLocalVariable) Type.LOCAL
        else if (isCompileTimeConstant(element)) Type.CONST
        else Type.FIELD
    }

    private fun oldPrefix(element: PsiVariable): String {
        val test = oldPrefixes[variableType(element)]
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
            if (element is PsiLocalVariable || element is PsiField)
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

const val LOCAL_VAR_PREFIX: String = "LOCAL_VAR_PREFIX"
const val LOCAL_PREV_VAR_PREFIX: String = "LOCAL_PREV_VAR_PREFIX"
const val LOCAL_CONST_PREFIX: String = "LOCAL_CONST_PREFIX"
const val LOCAL_PREV_CONST_PREFIX: String = "LOCAL_PREV_CONST_PREFIX"
const val LOCAL_FIELD_PREFIX: String = "LOCAL_FIELD_PREFIX"
const val LOCAL_PREV_FIELD_PREFIX: String = "LOCAL_PREV_FIELD_PREFIX"