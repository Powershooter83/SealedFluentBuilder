package me.prouge.sealedfluentbuilder.panels;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiField;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBRadioButton;
import me.prouge.sealedfluentbuilder.utils.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.intellij.ui.ToolbarDecorator.createDecorator;

public class FieldArrangementPanel extends DialogWrapper {

    final PluginContext context;
    private final JBList<PsiField> fieldList;
    private final JBList<PsiField> optionalFieldList;
    private final DefaultListModel<PsiField> fieldListModel;
    private final DefaultListModel<PsiField> optionalFieldListModel;
    private final List<JBRadioButton> radioButtons = new ArrayList<>();

    public FieldArrangementPanel(final PluginContext context, DefaultListModel<PsiField> fields) {
        super(context.ownerClass().getProject());
        this.context = context;
        radioButtons.add(new JBRadioButton(I18n.getMessage(Message.ARRANGEMENT_RADIO_BUTTON_CONSTRUCTOR)));
        radioButtons.add(new JBRadioButton(I18n.getMessage(Message.ARRANGEMENT_RADIO_BUTTON_CONSTRUCTOR_WITH_BUILDER)));
        radioButtons.add(new JBRadioButton(I18n.getMessage(Message.ARRANGEMENT_RADIO_BUTTON_CONSTRUCTOR_WITH_SETTER)));
        radioButtons.get(0).setSelected(true);

        setSize(600, 800);
        setTitle(I18n.getMessage(Message.ARRANGEMENT_TITLE));

        fieldListModel = fields;
        optionalFieldListModel = new DefaultListModel<>();
        fieldList = createPsiFieldList(fieldListModel);
        optionalFieldList = createPsiFieldList(optionalFieldListModel);
        init();
    }

    private JBList<PsiField> createPsiFieldList(final DefaultListModel<PsiField> fieldListModel) {
        final JBList<PsiField> fieldList = new JBList<>(fieldListModel);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        fieldList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        return fieldList;
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

        panel.add(createToolbar(fieldList)
                .addExtraAction(new AnAction(I18n.getMessage(Message.BUTTON_DOWN_LABEL)) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        moveFields(fieldList, fieldListModel, optionalFieldListModel);
                    }
                }).createPanel());

        panel.add(Box.createVerticalStrut(10));
        panel.add(createLeftAlignedBoxWithText(I18n.getMessage(Message.OPTIONAL_FIELDS_LABEL)));
        panel.add(Box.createVerticalStrut(10));

        panel.add(createToolbar(optionalFieldList)
                .addExtraAction(new AnAction(I18n.getMessage(Message.BUTTON_UP_LABEL)) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        moveFields(optionalFieldList, optionalFieldListModel, fieldListModel);
                    }
                }).createPanel());

        JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioButtons.forEach(radioButton -> {
            radioButtonPanel.add(radioButton);
            radioButton.addActionListener(e -> radioButtons.forEach(rb -> {
                if (rb != radioButton) {
                    rb.setSelected(false);
                }
            }));
        });

        panel.add(radioButtonPanel);
        return panel;
    }

    private ToolbarDecorator createToolbar(JList<PsiField> fieldList) {
        return createDecorator(fieldList)
                .disableAddAction()
                .disableRemoveAction();
    }

    @Override
    protected void doOKAction() {
        List<PsiField> selectedFields = getFields();

        if (selectedFields.size() < 2) {
            Messages.showErrorDialog(I18n.getMessage(Message.REQUIRED_FIELDS_ERROR), "");
            return;
        }

        super.doOKAction();
        new CodeGenerator(context, selectedFields, getOptionalFields(), getGenerationType());
    }

    private GenerationType getGenerationType() {
        if (radioButtons.get(0).isSelected()) {
            return GenerationType.CONSTRUCTOR;
        }
        if (radioButtons.get(1).isSelected()) {
            return GenerationType.CONSTRUCTOR_WITH_BUILDER;
        }
        return GenerationType.SETTER;
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
