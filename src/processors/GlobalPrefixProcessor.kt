package processors

import Renamer
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.util.Processor

class GlobalPrefixProcessor : Processor<PsiElement> {
    override fun process(p0: PsiElement?): Boolean {
        p0?.containingFile?.accept(object : JavaRecursiveElementVisitor() {
            val ren = Renamer.getInstance(p0.project)
            override fun visitLocalVariable(variable: PsiLocalVariable) {
                super.visitLocalVariable(variable)
                ren.addPrefix(variable)
            }

            override fun visitField(field: PsiField) {
                super.visitField(field)
                if (!ren.checkValidity(field)) ren.addPrefix(field)
            }

        })
        return true
    }
}