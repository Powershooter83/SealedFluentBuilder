package me.prouge.sealedfluentbuilder.utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import me.prouge.sealedfluentbuilder.ui.AppSettingsState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodeGenerator {

    private final PluginContext context;
    private final List<PsiField> requiredFields;
    private final List<PsiField> optionalFields;

    private final GenerationType generationType;

    private final String prefix;

    public CodeGenerator(final PluginContext context, final List<PsiField> requiredFields, final List<PsiField> optionalFields, final GenerationType generationType) {
        this.context = context;
        this.requiredFields = requiredFields;
        this.optionalFields = optionalFields;
        this.generationType = generationType;

        this.prefix = Objects.requireNonNull(AppSettingsState.getInstance().getState()).prefix.trim();

        generateBuilderCode();
    }

    private void generateBuilderCode() {
        Caret primaryCaret = context.editor().getCaretModel().getPrimaryCaret();
        if (!isCursorInsideClass(primaryCaret.getOffset())) {
            insertGeneratedCode(context.ownerClass().getLastChild().getTextOffset(), generateCode());
        } else {
            insertGeneratedCode(primaryCaret.getOffset(), generateCode());
        }
        formatInsertedCode();
    }

    private boolean isCursorInsideClass(int offset) {
        PsiFile psiFile = PsiDocumentManager.getInstance(context.project()).getPsiFile(context.editor().getDocument());
        PsiElement element = Objects.requireNonNull(psiFile).findElementAt(offset);
        if (element != null) {
            PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class, false);
            return psiClass != null;
        }
        return false;
    }

    private void insertGeneratedCode(int offset, String code) {
        WriteCommandAction.runWriteCommandAction(context.project(), () -> context.editor().getDocument().insertString(offset, code));
    }

    private void formatInsertedCode() {
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(context.project());
        documentManager.doPostponedOperationsAndUnblockDocument(context.editor().getDocument());
        new ReformatCodeProcessor(context.project(), context.ownerClass().getContainingFile(), null, false).run();
    }

    private String generatePublicBuilder() {
        final String fieldName = requiredFields.get(0).getName();
        return "public static " + toUppercase(requiredFields.get(1).getName()) + "Builder"
                + " "
                + getFieldNameFormatted(fieldName)
                + "(final " +
                requiredFields.get(0).getType().getPresentableText() +
                " " +
                fieldName +
                ") {\n" +
                "return new Builder()." +
                getFieldNameFormatted(fieldName) + "(" +
                fieldName + ");\n}\n";
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
                    .append(getFieldNameFormatted(field.getName()))
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
                .append(getFieldNameFormatted(fieldName))
                .append("(final ")
                .append(requiredFields.get(requiredFields.size() - 1).getType().getPresentableText())
                .append(" ")
                .append(fieldName)
                .append(");\n\n}");

        return code.toString();
    }


    private String optionalInterfaceBuilder() {
        final String creatorName = context.ownerClass().getName() + "Creator";
        return Stream.concat(
                        Stream.of("public sealed interface " + creatorName + " permits Builder {"),
                        optionalFields.stream()
                                .map(field -> String.format(
                                        "%s %s(final %s %s);",
                                        creatorName,
                                        getFieldNameFormatted(field.getName()),
                                        field.getType().getPresentableText(),
                                        field.getName()
                                ))
                )
                .collect(Collectors.joining("\n\n", "", "\n\n" + context.ownerClass().getName() + " build();\n\n}\n"));
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

        code.append("""
                private Builder() {
                }
                
                private %sBuilder %s(final %s %s) {
                    this.%s = %s;
                    return this;
                }
                """.formatted(
                toUppercase(requiredFields.get(1).getName()),
                getFieldNameFormatted(requiredFields.get(0).getName()),
                requiredFields.get(0).getType().getPresentableText(),
                requiredFields.get(0).getName(),
                requiredFields.get(0).getName(),
                requiredFields.get(0).getName()
        ));

        IntStream.range(1, requiredFields.size() - 1).forEach(i -> {
            PsiField field = requiredFields.get(i);
            PsiField nextField = requiredFields.get(i + 1);

            code.append("""
                    @Override
                    public %sBuilder %s(final %s %s) {
                        this.%s = %s;
                        return this;
                    }
                    """.formatted(
                    toUppercase(nextField.getName()),
                    getFieldNameFormatted(field.getName()),
                    field.getType().getPresentableText(),
                    field.getName(),
                    field.getName(),
                    field.getName()
            ));
        });

        final String creatorName = context.ownerClass().getName() + "Creator";
        final PsiField lastField = requiredFields.get(requiredFields.size() - 1);

        code.append("""
                @Override
                public %s %s(final %s %s) {
                    this.%s = %s;
                    return this;
                }
                """.formatted(
                creatorName,
                getFieldNameFormatted(lastField.getName()),
                lastField.getType().getPresentableText(),
                lastField.getName(),
                lastField.getName(),
                lastField.getName()
        ));

        for (final PsiField field : optionalFields) {
            code.append("""
                    @Override
                    public %s %s(final %s %s) {
                        this.%s = %s;
                        return this;
                    }
                    """.formatted(
                    creatorName,
                    getFieldNameFormatted(field.getName()),
                    field.getType().getPresentableText(),
                    field.getName(),
                    field.getName(),
                    field.getName()
            ));
        }


        final String className = toLowercase(context.ownerClass().getName());

        switch (generationType) {
            case SETTER -> code.append(generateWithSetter(className));
            case CONSTRUCTOR -> code.append(generateWithConstructor());
            case CONSTRUCTOR_WITH_BUILDER -> code.append(generateWithConstructorBuilder());
        }
        return code.toString();
    }

    private String generateSetter() {
        StringBuilder code = new StringBuilder();
        getAllFields().forEach(field -> {
            final String fieldName = field.getName();
            final String fieldType = field.getType().getPresentableText();
            final String setterName = "set" + toUppercase(fieldName);

            code.append(String.format("public void %s(final %s %s) {\n", setterName, fieldType, fieldName));
            code.append(String.format("    this.%s = %s;\n", fieldName, fieldName));
            code.append("}\n");
        });

        return code.toString();
    }

    private String generateConstructor() {
        List<PsiField> allFields = getAllFields();
        StringBuilder code = new StringBuilder();

        final String modifier = AppSettingsState.getInstance().getConstructorModifier().toString().toLowerCase();

        code.append(String.format(modifier + " %s(", context.ownerClass().getName()));
        for (int i = 0; i < allFields.size() - 1; i++) {
            code.append(String.format("final %s %s, ", allFields.get(i).getType().getPresentableText(), allFields.get(i).getName()));
        }
        code.append(String.format("final %s %s", allFields.get(allFields.size() - 1).getType().getPresentableText(), allFields.get(allFields.size() - 1).getName()));
        code.append(") {\n");
        for (PsiField field : allFields) {
            code.append(String.format("    this.%s = %s;\n", field.getName(), field.getName()));
        }
        code.append("}\n");
        return code.toString();
    }

    private String generateConstructorBuilder() {
        StringBuilder code = new StringBuilder();
        List<PsiField> allFields = getAllFields();
        final String modifier = AppSettingsState.getInstance().getConstructorWithBuilderModifier().toString().toLowerCase();
        code.append(String.format(modifier + " %s(final Builder builder) {\n", context.ownerClass().getName()));
        allFields.forEach(field -> code.append(String.format("this.%s = builder.%s;\n", field.getName(), field.getName())));
        code.append("}\n");
        return code.toString();
    }

    private String generateWithConstructor() {
        StringBuilder code = new StringBuilder();
        List<PsiField> allFields = getAllFields();
        code.append("""
                @Override
                public %s build() {
                    return new %s(""".formatted(
                context.ownerClass().getName(), context.ownerClass().getName()));

        for (int i = 0; i < allFields.size() - 1; i++) {
            code.append("%s, ".formatted(allFields.get(i).getName()));
        }
        code.append("%s);\n}\n".formatted(allFields.get(allFields.size() - 1).getName()));
        return code.toString();
    }

    private String generateWithConstructorBuilder() {
        return """
                @Override
                public %s build() {
                    return new %s(this);
                }
                """.formatted(
                context.ownerClass().getName(), context.ownerClass().getName());
    }

    private String generateWithSetter(final String className) {
        StringBuilder code = new StringBuilder();
        code.append("""
                @Override
                public %s build() {
                    final %s %s = new %s();
                """.formatted(
                context.ownerClass().getName(),
                context.ownerClass().getName(),
                className,
                context.ownerClass().getName()
        ));


        requiredFields.forEach(field -> code.append("""
                %s.set%s(%s);
                """.formatted(
                className,
                toUppercase(field.getName()),
                field.getName()
        )));

        optionalFields.forEach(field -> code.append("""
                %s.set%s(%s);
                """.formatted(
                className,
                toUppercase(field.getName()),
                field.getName()
        )));

        code.append("""
                return %s;
                }
                """.formatted(className));

        return code.toString();
    }

    private String toUppercase(final String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private String toLowercase(final String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    private List<PsiField> getAllFields() {
        List<PsiField> allFields = new ArrayList<>(requiredFields);
        allFields.addAll(optionalFields);
        return allFields;
    }


    private String generateCode() {
        StringBuilder code = new StringBuilder();

        switch (generationType) {
            case SETTER -> code.append(generateSetter());
            case CONSTRUCTOR -> code.append(generateConstructor());
            case CONSTRUCTOR_WITH_BUILDER -> code.append(generateConstructorBuilder());
        }
        return code.append(generatePublicBuilder())
                .append(requiredInterfaceBuilder())
                .append(optionalInterfaceBuilder())
                .append(builderClassBuilder())
                .append("\n}\n").toString();
    }

    private String getFieldNameFormatted(String name) {
        if (prefix.isBlank()) {
            return name;
        }
        return prefix + capitalizeFirstLetter(name);
    }


    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}


