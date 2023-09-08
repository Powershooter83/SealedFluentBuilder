package me.prouge.sealedFluentBuilder.actions;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import me.prouge.sealedFluentBuilder.panels.FieldSelectionPanel;

public class CreateBuilderAction extends AnAction {

   @Override
   public void actionPerformed(@NotNull AnActionEvent event) {
      Project project = event.getProject();
      Editor editor = event.getData(PlatformDataKeys.EDITOR);
      PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);

      FieldSelectionPanel panel = new FieldSelectionPanel(project, editor, psiClass);
      panel.show();
   }

}
