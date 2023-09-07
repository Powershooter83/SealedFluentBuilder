package me.prouge.fluentbuilder.utils;

import java.util.List;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiField;

public class CodeGenerator {

   public static final String BUILDER = "Builder";

   private static List<PsiField> requiredFields;

   private static List<PsiField> optionalFields;

   private static PsiClass ownerClass;

   public static void generateBuilderCode(Project project, Editor editor, PsiClass psiClass, List<PsiField> requiredFields,
         List<PsiField> optionalFields) {
      ownerClass = psiClass;
      Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();

      CodeGenerator.requiredFields = requiredFields;
      CodeGenerator.optionalFields = optionalFields;

      Runnable formatCodeRunnable = () -> {
         PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
         documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());
         new ReformatCodeProcessor(project, psiClass.getContainingFile(), null, false).run();
      };

      WriteCommandAction.runWriteCommandAction(project, () -> {

         editor.getDocument().insertString(primaryCaret.getOffset(), generateCode());
         formatCodeRunnable.run();
      });

   }

   private static String generatePublicBuilder() {
      StringBuilder code = new StringBuilder();
      final String fieldName = requiredFields.get(0).getName();

      code.append("public static ").append(toUppercase(requiredFields.get(1).getName())).append(BUILDER).append(" id(final ")
            .append(requiredFields.get(0).getType().getPresentableText())
            .append(" ")
            .append(fieldName)
            .append(") {\n")
            .append("return new Builder().")
            .append(fieldName).append("(")
            .append(fieldName).append(");\n}\n");
      return code.toString();
   }

   private static String requiredInterfaceBuilder() {
      StringBuilder code = new StringBuilder();

      for (int i = 1; i < requiredFields.size() - 1; i++) {
         PsiField field = requiredFields.get(i);
         PsiField nextField = requiredFields.get(i + 1);

         code.append("public sealed interface ")
               .append(toUppercase(field.getName()))
               .append("Builder permits Builder {\n\n")
               .append(toUppercase(nextField.getName()))
               .append("Builder")
               .append(" ")
               .append(field.getName())
               .append("(final ")
               .append(field.getType().getPresentableText())
               .append(" ")
               .append(field.getName())
               .append(");")
               .append("\n\n}\n");
      }
      String fieldName = requiredFields.get(requiredFields.size() - 1).getName();

      code.append("public sealed interface ")
            .append(toUppercase(fieldName))
            .append("Builder")
            .append(" permits Builder {\n\n")
            .append(ownerClass.getName())
            .append("Creator ")
            .append(fieldName)
            .append("(final ")
            .append(requiredFields.get(requiredFields.size() - 1).getType().getPresentableText())
            .append(" ")
            .append(fieldName)
            .append(");\n\n}");

      return code.toString();
   }

   private static String optionalInterfaceBuilder() {
      StringBuilder code = new StringBuilder();

      final String creatorName = ownerClass.getName() + "Creator";

      code.append("public sealed interface ")
            .append(creatorName)
            .append(" permits Builder {\n\n");

      for (PsiField field : optionalFields) {
         code.append(creatorName)
               .append(" ")
               .append(field.getName())
               .append("(final ")
               .append(field.getType().getPresentableText())
               .append(" ")
               .append(field.getName())
               .append("); \n\n");
      }

      code.append(ownerClass.getName())
            .append(" build(); \n\n }");
      return code.toString();
   }

   private static String builderClassBuilder() {
      StringBuilder code = new StringBuilder();
      code.append("private static final class Builder implements ");
      for (int i = 1; i < requiredFields.size(); i++) {
         code.append(toUppercase(requiredFields.get(i).getName()))
               .append("Builder, ");
      }
      code.append(ownerClass.getName())
            .append("Creator {\n\n");

      for (PsiField field : requiredFields) {
         code.append("private ")
               .append(field.getType().getPresentableText())
               .append(" ")
               .append(field.getName())
               .append(";\n\n");
      }

      for (PsiField field : optionalFields) {
         code.append("private ")
               .append(field.getType().getPresentableText())
               .append(" ")
               .append(field.getName())
               .append(";\n\n");
      }

      code.append("private Builder() {\n}\n\n");

      code.append("private ")
            .append(toUppercase(requiredFields.get(1).getName()))
            .append("Builder")
            .append(" ")
            .append(requiredFields.get(0).getName())
            .append("(final ")
            .append(requiredFields.get(0).getType().getPresentableText())
            .append(" ")
            .append(requiredFields.get(0).getName())
            .append(") {")
            .append("this.")
            .append(requiredFields.get(0).getName())
            .append(" = ")
            .append(requiredFields.get(0).getName())
            .append(";\nreturn this;\n}\n");

      for (int i = 1; i < requiredFields.size() - 1; i++) {
         final PsiField field = requiredFields.get(i);
         final PsiField nextField = requiredFields.get(i + 1);

         code.append("@Override\n")
               .append("public ")
               .append(toUppercase(nextField.getName()))
               .append("Builder ")
               .append(field.getName())
               .append("(final ")
               .append(field.getType().getPresentableText())
               .append(" ")
               .append(field.getName())
               .append(") {\n")
               .append("this.")
               .append(field.getName())
               .append(" = ")
               .append(field.getName())
               .append(";\n")
               .append("return this;")
               .append("\n}\n");
      }

      final String creatorName = ownerClass.getName() + "Creator";
      final PsiField lastField = requiredFields.get(requiredFields.size() - 1);

      code.append("@Override\n")
            .append("public ")
            .append(creatorName)
            .append(" ")
            .append(lastField.getName())
            .append("(final ")
            .append(lastField.getType().getPresentableText())
            .append(" ")
            .append(lastField.getName())
            .append(") {\n")
            .append("this.")
            .append(lastField.getName())
            .append(" = ")
            .append(lastField.getName())
            .append(";\n")
            .append("return this;")
            .append("\n}\n");

      for (final PsiField field : optionalFields) {
         code.append("@Override\n")
               .append("public ")
               .append(creatorName)
               .append(" ")
               .append(field.getName())
               .append("(final ")
               .append(field.getType().getPresentableText())
               .append(" ")
               .append(field.getName())
               .append(") {\n")
               .append("this.")
               .append(field.getName())
               .append(" = ")
               .append(field.getName())
               .append(";\n")
               .append("return this;")
               .append("\n}\n");
      }

      final String className = toLowercase(ownerClass.getName());

      code.append("@Override")
            .append("\npublic ")
            .append(ownerClass.getName())
            .append(" build() {\n")
            .append("final ")
            .append(ownerClass.getName())
            .append(" ")
            .append(className)
            .append(" = new ")
            .append(ownerClass.getName())
            .append("();\n");

      for (PsiField field : requiredFields) {
         code.append(className).append(".set")
               .append(toUppercase(field.getName()))
               .append("(")
               .append(field.getName())
               .append(");\n");
      }
      for (PsiField field : optionalFields) {
         code.append(className).append(".set")
               .append(toUppercase(field.getName()))
               .append("(")
               .append(field.getName())
               .append(");\n");
      }
      code.append("return ").append(className).append(";");

      code.append("\n}\n");

      return code.toString();
   }

   private static String toUppercase(final String value) {
      return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
   }

   private static String toLowercase(final String value) {
      return value.substring(0, 1).toLowerCase() + value.substring(1);
   }

   public static String generateCode() {
      StringBuilder code = new StringBuilder();
      code.append(generatePublicBuilder());
      code.append(requiredInterfaceBuilder());
      code.append(optionalInterfaceBuilder());
      code.append(builderClassBuilder());
      code.append("\n}\n");
      return code.toString();
   }

}


