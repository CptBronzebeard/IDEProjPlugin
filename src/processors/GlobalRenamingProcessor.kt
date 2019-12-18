package processors

import Renamer
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.refactoring.RefactoringFactory
import com.intellij.util.Processor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiUtil.*

class GlobalRenamingProcessor : Processor<PsiElement> {

    override fun process(p0: PsiElement?): Boolean {
        p0?.containingFile?.accept(object : JavaRecursiveElementVisitor() {

            var srv: Renamer? = null

            override fun visitLocalVariable(variable: PsiLocalVariable) {
                super.visitLocalVariable(variable)
                if (srv == null) {
                    srv = Renamer.getInstance(p0.project)
                }
                if (!variable.name.startsWith(srv!!.localPrefix)) {
                    RefactoringFactory.getInstance(p0.project).createRename(variable,
                            srv!!.localPrefix + variable.name.removePrefix(srv!!.prevLocal)).run()
                }
            }

            override fun visitField(field: PsiField) {
                super.visitField(field)
                if (srv == null) {
                    srv = Renamer.getInstance(p0.project)
                }
                if (!field.name.startsWith(srv!!.constPrefix)) {
                    if (isCompileTimeConstant(field)) RefactoringFactory.getInstance(p0.project).createRename(field,
                            srv!!.constPrefix + field.name.removePrefix(srv!!.prevConst)).run()
                }
            }
        })
        return true
    }
}