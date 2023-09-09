package me.prouge.sealedfluentbuilder.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import me.prouge.sealedfluentbuilder.panels.FieldSelectionPanel;
import me.prouge.sealedfluentbuilder.utils.PluginContext;
import org.jetbrains.annotations.NotNull;

public class CreateBuilderAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        if (project == null || editor == null) {
            return;
        }

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        PsiClass psiClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);

        if (psiClass == null) {
            return;
        }

        new FieldSelectionPanel(new PluginContext(project, editor, psiClass)).show();
    }

}
