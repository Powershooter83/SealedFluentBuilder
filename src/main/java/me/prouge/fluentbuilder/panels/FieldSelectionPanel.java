package me.prouge.fluentbuilder.panels;

import static com.intellij.openapi.ui.LabeledComponent.create;
import static com.intellij.ui.ToolbarDecorator.createDecorator;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;

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

public class FieldSelectionPanel extends DialogWrapper {

   private final JBList<PsiField> fieldList;

   final PsiClass ownerClass;

   final Project project;

   final Editor editor;

   public FieldSelectionPanel(final Project project, final Editor editor, final PsiClass ownerClass) {
      super(ownerClass.getProject());
      this.project = project;
      this.editor = editor;
      this.ownerClass = ownerClass;
      fieldList = loadClassFields(ownerClass);
      setSize(600, 400);
      setTitle("Select fields for the fluent builder (optional fields can be explicitly selected later)");
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
      if(getFields().size() < 2) {
         Messages.showErrorDialog("At least two fields must be selected!", "");
      }else {
         super.doOKAction();
         new FieldArrangementPanel(project, editor, ownerClass, getFields()).show();
      }

   }

   @Override
   protected JComponent createCenterPanel() {
      return create(createToolbar().createPanel(), "Available fields");
   }

   public DefaultListModel<PsiField> getFields() {
      DefaultListModel<PsiField> model = new DefaultListModel<>();
      for (PsiField field : fieldList.getSelectedValuesList()) {
         model.addElement(field);
      }
      return model;
   }

}