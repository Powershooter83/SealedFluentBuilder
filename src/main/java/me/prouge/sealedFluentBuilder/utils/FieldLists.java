package me.prouge.sealedFluentBuilder.utils;

import com.intellij.psi.PsiField;

import java.util.List;

public record FieldLists(List<PsiField> requiredFields, List<PsiField> optionalFields) {
}
