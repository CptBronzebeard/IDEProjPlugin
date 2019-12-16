package processors

import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.refactoring.RefactoringFactory
import com.intellij.util.Processor
import com.intellij.psi.util.PsiUtil.*

class GlobalRenamingProcessor : Processor<PsiElement> {
    override fun process(p0: PsiElement?): Boolean {
        p0?.containingFile?.accept(object : JavaRecursiveElementVisitor() {
            override fun visitLocalVariable(variable: PsiLocalVariable) {
                super.visitLocalVariable(variable)
                RefactoringFactory.getInstance(p0.project).createRename(variable, "it_${variable.name}").run()
            }

            override fun visitField(field: PsiField) {
                super.visitField(field)
                if (isCompileTimeConstant(field)) RefactoringFactory.getInstance(p0.project).createRename(field, "c_${field.name}").run()
            }
        })
        return true
    }
}