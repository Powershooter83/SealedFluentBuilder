package me.prouge.sealedFluentBuilder.utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiField;

import java.util.List;

public class CodeGenerator {

    public static final String BUILDER = "Builder";
    private final PluginContext context;
    private final List<PsiField> requiredFields;
    private final List<PsiField> optionalFields;

    public CodeGenerator(final PluginContext context, final List<PsiField> requiredFields, final List<PsiField> optionalFields) {
        this.context = context;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;

        generateBuilderCode();
    }

    private void generateBuilderCode() {
        Caret primaryCaret = context.editor().getCaretModel().getPrimaryCaret();

        Runnable formatCodeRunnable = () -> {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(context.project());
            documentManager.doPostponedOperationsAndUnblockDocument(context.editor().getDocument());
            new ReformatCodeProcessor(context.project(), context.ownerClass().getContainingFile(), null, false).run();
        };

        WriteCommandAction.runWriteCommandAction(context.project(), () -> {

            context.editor().getDocument().insertString(primaryCaret.getOffset(), generateCode());
            formatCodeRunnable.run();
        });

    }

    private String generatePublicBuilder() {
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

    private String requiredInterfaceBuilder() {
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
                .append(context.ownerClass().getName())
                .append("Creator ")
                .append(fieldName)
                .append("(final ")
                .append(requiredFields.get(requiredFields.size() - 1).getType().getPresentableText())
                .append(" ")
                .append(fieldName)
                .append(");\n\n}");

        return code.toString();
    }

    private String optionalInterfaceBuilder() {
        StringBuilder code = new StringBuilder();

        final String creatorName = context.ownerClass().getName() + "Creator";

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

        code.append(context.ownerClass().getName())
                .append(" build(); \n\n }");
        return code.toString();
    }

    private String builderClassBuilder() {
        StringBuilder code = new StringBuilder();
        code.append("private static final class Builder implements ");
        for (int i = 1; i < requiredFields.size(); i++) {
            code.append(toUppercase(requiredFields.get(i).getName()))
                    .append("Builder, ");
        }
        code.append(context.ownerClass().getName())
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

        final String creatorName = context.ownerClass().getName() + "Creator";
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

        final String className = toLowercase(context.ownerClass().getName());

        code.append("@Override")
                .append("\npublic ")
                .append(context.ownerClass().getName())
                .append(" build() {\n")
                .append("final ")
                .append(context.ownerClass().getName())
                .append(" ")
                .append(className)
                .append(" = new ")
                .append(context.ownerClass().getName())
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

    private String toUppercase(final String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private String toLowercase(final String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    private String generateCode() {
        return generatePublicBuilder() +
                requiredInterfaceBuilder() +
                optionalInterfaceBuilder() +
                builderClassBuilder() +
                "\n}\n";
    }

}


