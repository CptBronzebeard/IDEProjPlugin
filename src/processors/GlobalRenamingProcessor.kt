package processors

import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLocalVariable
import com.intellij.refactoring.RefactoringFactory
import com.intellij.util.Processor

class GlobalRenamingProcessor : Processor<PsiElement> {
    override fun process(p0: PsiElement?): Boolean {
        p0?.containingFile?.accept(object : JavaRecursiveElementVisitor() {
            override fun visitLocalVariable(variable: PsiLocalVariable) {
                super.visitLocalVariable(variable)
                RefactoringFactory.getInstance(p0.project).createRename(variable, "it_${variable.name}").run()
            }
        })
        return true
    }
}