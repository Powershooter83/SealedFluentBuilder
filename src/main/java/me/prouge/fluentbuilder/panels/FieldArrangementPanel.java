package me.prouge.fluentbuilder.panels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.NotNull;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import me.prouge.fluentbuilder.utils.CodeGenerator;

public class FieldArrangementPanel extends DialogWrapper {

   private final JList<PsiField> fieldList;

   private final JList<PsiField> optionalFieldList;

   private final DefaultListModel<PsiField> fieldListModel;

   private final DefaultListModel<PsiField> optionalFieldListModel;

   final Project project;

   final Editor editor;

   final PsiClass ownerClass;

   public FieldArrangementPanel(final Project project, final Editor editor,
         final PsiClass ownerClass, DefaultListModel<PsiField> fields) {
      super(ownerClass.getProject());
      this.project = project;
      this.editor = editor;
      this.ownerClass = ownerClass;

      setSize(600, 800);
      setTitle("Select optional fields (the order is respected by the builder)");

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

      panel.add(createLeftAlignedBoxWithText("Required Fields:"));
      panel.add(Box.createVerticalStrut(10));
      ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
      decorator.disableRemoveAction();
      decorator.addExtraAction(new AnActionButton("Move Down", AllIcons.Actions.NextOccurence) {

         @Override
         public void actionPerformed(@NotNull AnActionEvent e) {
            moveFields(fieldList, fieldListModel, optionalFieldListModel);
         }
      });

      panel.add(decorator.createPanel());
      panel.add(Box.createVerticalStrut(10));

      panel.add(createLeftAlignedBoxWithText("Optional Fields:"));
      panel.add(Box.createVerticalStrut(10));

      ToolbarDecorator optionalDecorator = ToolbarDecorator.createDecorator(optionalFieldList);
      optionalDecorator.disableRemoveAction();
      optionalDecorator.addExtraAction(new AnActionButton("Move Up", AllIcons.Actions.PreviousOccurence) {

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
      if(getFields().size() == 1) {
         Messages.showErrorDialog("At least two fields must be selected as required!", "");
      }else {
         super.doOKAction();
         CodeGenerator.generateBuilderCode(project,
               editor,
               ownerClass,
               getFields(), getOptionalFields());
      }

   }

   private void moveFields(JList<PsiField> sourceList, DefaultListModel<PsiField> sourceModel, DefaultListModel<PsiField> targetModel) {
      List<PsiField> selectedFields = sourceList.getSelectedValuesList();
      for (PsiField field : selectedFields) {
         if (!targetModel.contains(field)) {
            targetModel.addElement(field);
         }
         sourceModel.removeElement(field);
      }
   }

   private List<PsiField> getSelectedFieldsFromList(JList<PsiField> list) {
      List<PsiField> selectedFields = new ArrayList<>();
      ListModel<PsiField> model = list.getModel();
      for (int i = 0; i < model.getSize(); i++) {
         PsiField field = model.getElementAt(i);
         selectedFields.add(field);
      }
      return selectedFields;
   }

   public List<PsiField> getFields() {
      return getSelectedFieldsFromList(fieldList);
   }

   public List<PsiField> getOptionalFields() {
      return getSelectedFieldsFromList(optionalFieldList);
   }

}
