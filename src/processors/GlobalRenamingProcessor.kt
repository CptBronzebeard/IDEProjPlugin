package processors

import Renamer
import com.intellij.psi.*
import com.intellij.util.Processor

class GlobalRenamingProcessor : Processor<PsiElement> {

    override fun process(p0: PsiElement?): Boolean {
        p0?.containingFile?.accept(object : JavaRecursiveElementVisitor() {

            var srv: Renamer? = null
            override fun visitClass(aClass: PsiClass?) {
                if (srv == null) {
                    srv = Renamer.getInstance(p0.project)
                }
                srv!!.restyleGlobal(aClass!!.originalElement)
                super.visitClass(aClass)
            }

            override fun visitLocalVariable(variable: PsiLocalVariable) {
                super.visitLocalVariable(variable)
                if (srv == null) {
                    srv = Renamer.getInstance(p0.project)
                }
                srv!!.prefixGlobal(variable)
                srv!!.restyleGlobal(variable)
            }

            override fun visitField(field: PsiField) {
                super.visitField(field)
                if (srv == null) {
                    srv = Renamer.getInstance(p0.project)
                }
                srv!!.prefixGlobal(field)
                srv!!.restyleGlobal(field)
            }
        })
        return true
    }
}