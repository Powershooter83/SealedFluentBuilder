package me.prouge.sealedfluentbuilder.utils;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiField;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodeGenerator {

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

        insertGeneratedCode(primaryCaret.getOffset(), generateCode());
        formatInsertedCode();
    }

    private void insertGeneratedCode(int offset, String code) {
        WriteCommandAction.runWriteCommandAction(context.project(), () -> {
            context.editor().getDocument().insertString(offset, code);
        });
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
                + fieldName
                + "(final " +
                requiredFields.get(0).getType().getPresentableText() +
                " " +
                fieldName +
                ") {\n" +
                "return new Builder()." +
                fieldName + "(" +
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
        final String creatorName = context.ownerClass().getName() + "Creator";
        return Stream.concat(
                        Stream.of("public sealed interface " + creatorName + " permits Builder {"),
                        optionalFields.stream()
                                .map(field -> String.format(
                                        "%s %s(final %s %s);",
                                        creatorName,
                                        field.getName(),
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
                requiredFields.get(0).getName(),
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
                    field.getName(),
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
                lastField.getName(),
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
                    field.getName(),
                    field.getType().getPresentableText(),
                    field.getName(),
                    field.getName(),
                    field.getName()
            ));
        }


        final String className = toLowercase(context.ownerClass().getName());

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

    private String generateCode() {
        return generatePublicBuilder() +
                requiredInterfaceBuilder() +
                optionalInterfaceBuilder() +
                builderClassBuilder() +
                "\n}\n";
    }

}


