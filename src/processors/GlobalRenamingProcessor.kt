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
                val prefix = srv!!.localPrefix
                if (prefix.isEmpty() || !variable.name.startsWith(prefix)) {
                    RefactoringFactory.getInstance(p0.project).createRename(variable,
                            prefix + variable.name.removePrefix(srv!!.prevLocal)).run()
                }
                if (!srv!!.isStyled(variable)) {
                    RefactoringFactory.getInstance(p0.project).createRename(variable, srv!!.getRenamer()!!(variable)).run()
                }
            }

            override fun visitField(field: PsiField) {
                super.visitField(field)
                if (srv == null) {
                    srv = Renamer.getInstance(p0.project)
                }
                val prefix = if (isCompileTimeConstant(field)) srv!!.constPrefix else srv!!.fieldPrefix
                val prevPrefix = if (isCompileTimeConstant(field)) srv!!.prevConst else srv!!.prevField
                if (prefix.isEmpty() || !field.name.startsWith(prefix)) {
                    RefactoringFactory.getInstance(p0.project).createRename(field,
                            prefix + field.name.removePrefix(prevPrefix)).run()
                }
                if (!srv!!.isStyled(field)) {
                    RefactoringFactory.getInstance(p0.project).createRename(field, srv!!.getRenamer()!!(field)).run()
                }
            }
        })
        return true
    }
}