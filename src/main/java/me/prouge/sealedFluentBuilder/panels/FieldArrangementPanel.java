package me.prouge.sealedFluentBuilder.panels;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import me.prouge.sealedFluentBuilder.utils.CodeGenerator;
import me.prouge.sealedFluentBuilder.utils.I18n;
import me.prouge.sealedFluentBuilder.utils.Message;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FieldArrangementPanel extends DialogWrapper {

    final Project project;
    final Editor editor;
    final PsiClass ownerClass;
    private final JList<PsiField> fieldList;
    private final JList<PsiField> optionalFieldList;
    private final DefaultListModel<PsiField> fieldListModel;
    private final DefaultListModel<PsiField> optionalFieldListModel;

    public FieldArrangementPanel(final Project project, final Editor editor,
                                 final PsiClass ownerClass, DefaultListModel<PsiField> fields) {
        super(ownerClass.getProject());
        this.project = project;
        this.editor = editor;
        this.ownerClass = ownerClass;

        setSize(600, 800);
        setTitle(I18n.getMessage(Message.ARRANGEMENT_TITLE));

        fieldListModel = fields;
        fieldList = new JBList<>(fieldListModel);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        fieldList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        optionalFieldListModel = new DefaultListModel<>();
        optionalFieldList = new JBList<>(optionalFieldListModel);
        optionalFieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        optionalFieldList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        init();
    }

    @Override
    protected void init() {
        super.init();
    }

    private Box createLeftAlignedBoxWithText(String text) {
        Box box = Box.createHorizontalBox();
        box.add(new JLabel(text, SwingConstants.LEFT));
        box.add(Box.createHorizontalGlue());
        return box;
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createLeftAlignedBoxWithText(I18n.getMessage(Message.REQUIRED_FIELDS_LABEL)));
        panel.add(Box.createVerticalStrut(10));
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableRemoveAction();
        decorator.addExtraAction(new AnAction("Move Down") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                moveFields(fieldList, fieldListModel, optionalFieldListModel);
            }
        });

        panel.add(decorator.createPanel());
        panel.add(Box.createVerticalStrut(10));

        panel.add(createLeftAlignedBoxWithText(I18n.getMessage(Message.OPTIONAL_FIELDS_LABEL)));
        panel.add(Box.createVerticalStrut(10));

        ToolbarDecorator optionalDecorator = ToolbarDecorator.createDecorator(optionalFieldList);
        optionalDecorator.disableRemoveAction();
        optionalDecorator.addExtraAction(new AnAction("Move Up") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                moveFields(optionalFieldList, optionalFieldListModel, fieldListModel);
            }
        });

        panel.add(optionalDecorator.createPanel());

        return panel;
    }

    @Override
    protected void doOKAction() {
        List<PsiField> selectedFields = getFields();

        if (selectedFields.size() < 2) {
            Messages.showErrorDialog(I18n.getMessage(Message.REQUIRED_FIELDS_ERROR), "");
            return;
        }

        super.doOKAction();
        CodeGenerator.generateBuilderCode(project, editor, ownerClass, selectedFields, getOptionalFields());
    }

    private void moveFields(JList<PsiField> sourceList, DefaultListModel<PsiField> sourceModel, DefaultListModel<PsiField> targetModel) {
        List<PsiField> selectedFields = sourceList.getSelectedValuesList();
        selectedFields.stream()
                .filter(field -> !targetModel.contains(field))
                .forEach(targetModel::addElement);

        selectedFields.forEach(sourceModel::removeElement);
    }

    private List<PsiField> getSelectedFieldsFromList(JList<PsiField> list) {
        ListModel<PsiField> model = list.getModel();
        return IntStream.range(0, model.getSize())
                .mapToObj(model::getElementAt)
                .collect(Collectors.toList());
    }

    private List<PsiField> getFields() {
        return getSelectedFieldsFromList(fieldList);
    }

    private List<PsiField> getOptionalFields() {
        return getSelectedFieldsFromList(optionalFieldList);
    }

}
