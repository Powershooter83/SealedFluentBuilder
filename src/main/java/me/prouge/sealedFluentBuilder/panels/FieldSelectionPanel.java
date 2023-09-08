package me.prouge.sealedFluentBuilder.panels;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import me.prouge.sealedFluentBuilder.utils.I18n;
import me.prouge.sealedFluentBuilder.utils.Message;

import javax.swing.*;

import static com.intellij.openapi.ui.LabeledComponent.create;
import static com.intellij.ui.ToolbarDecorator.createDecorator;

public class FieldSelectionPanel extends DialogWrapper {

    final PsiClass ownerClass;
    final Project project;
    final Editor editor;
    private final JBList<PsiField> fieldList;

    public FieldSelectionPanel(final Project project, final Editor editor, final PsiClass ownerClass) {
        super(ownerClass.getProject());
        this.project = project;
        this.editor = editor;
        this.ownerClass = ownerClass;
        fieldList = loadClassFields(ownerClass);
        setSize(600, 400);
        setTitle(I18n.getMessage(Message.SELECTION_TITLE));
        init();
    }

    @Override
    protected void init() {
        super.init();
    }

    private JBList<PsiField> loadClassFields(final PsiClass ownerClass) {
        CollectionListModel<PsiField> fields = new CollectionListModel<>(ownerClass.getFields());
        JBList<PsiField> fieldsList = new JBList<>(fields);
        fieldsList.setCellRenderer(new DefaultPsiElementCellRenderer());
        return fieldsList;
    }

    private ToolbarDecorator createToolbar() {
        ToolbarDecorator decorator = createDecorator(fieldList);
        decorator.disableAddAction();
        decorator.disableRemoveAction();
        decorator.disableUpDownActions();
        return decorator;
    }

    @Override
    protected void doOKAction() {
        if (getFields().size() < 2) {
            Messages.showErrorDialog(I18n.getMessage(Message.SELECTED_FIELDS_ERROR), "");
        } else {
            super.doOKAction();
            new FieldArrangementPanel(project, editor, ownerClass, getFields()).show();
        }
    }

    @Override
    protected JComponent createCenterPanel() {
        return create(createToolbar().createPanel(), I18n.getMessage(Message.SELECTION_TITLE));
    }

    public DefaultListModel<PsiField> getFields() {
        DefaultListModel<PsiField> model = new DefaultListModel<>();
        for (PsiField field : fieldList.getSelectedValuesList()) {
            model.addElement(field);
        }
        return model;
    }

}