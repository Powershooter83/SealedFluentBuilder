package me.prouge.sealedfluentbuilder.panels;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import me.prouge.sealedfluentbuilder.utils.I18n;
import me.prouge.sealedfluentbuilder.utils.Message;
import me.prouge.sealedfluentbuilder.utils.PluginContext;

import javax.swing.*;

import static com.intellij.openapi.ui.LabeledComponent.create;
import static com.intellij.ui.ToolbarDecorator.createDecorator;

public class FieldSelectionPanel extends DialogWrapper {

    final PluginContext context;
    private final JBList<PsiField> fieldList;

    public FieldSelectionPanel(final PluginContext context) {
        super(context.ownerClass().getProject());

        this.context = context;
        fieldList = loadClassFields(context.ownerClass());
        selectAllFields();
        setSize(600, 400);
        setTitle(I18n.getMessage(Message.SELECTION_TITLE));
        init();
    }

    @Override
    protected void init() {
        super.init();
    }

    private void selectAllFields() {
        int[] indices = new int[fieldList.getModel().getSize()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        fieldList.setSelectedIndices(indices);
    }

    private JBList<PsiField> loadClassFields(final PsiClass ownerClass) {
        CollectionListModel<PsiField> fields = new CollectionListModel<>(ownerClass.getFields());
        JBList<PsiField> fieldsList = new JBList<>(fields);
        fieldsList.setCellRenderer(new DefaultPsiElementCellRenderer());
        return fieldsList;
    }

    private ToolbarDecorator createToolbar() {
        return createDecorator(fieldList)
                .disableAddAction()
                .disableRemoveAction()
                .disableUpDownActions();
    }

    @Override
    protected void doOKAction() {
        DefaultListModel<PsiField> selectedFields = getFields();

        if (selectedFields.size() < 2) {
            Messages.showErrorDialog(I18n.getMessage(Message.SELECTED_FIELDS_ERROR), "");
            return;
        }

        super.doOKAction();
        new FieldArrangementPanel(context, selectedFields).show();
    }

    @Override
    protected JComponent createCenterPanel() {
        return create(createToolbar().createPanel(), I18n.getMessage(Message.SELECTION_TITLE));
    }

    public DefaultListModel<PsiField> getFields() {
        DefaultListModel<PsiField> model = new DefaultListModel<>();
        fieldList.getSelectedValuesList().forEach(model::addElement);
        return model;
    }

}