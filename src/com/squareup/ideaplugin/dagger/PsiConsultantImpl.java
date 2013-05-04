package com.squareup.ideaplugin.dagger;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;

public class PsiConsultantImpl {

  static PsiAnnotation findInjectAnnotation(PsiField field) {
    PsiModifierList modifiers = field.getModifierList();
    if (modifiers != null) {
      PsiAnnotation[] annotations = modifiers.getAnnotations();
      for (PsiAnnotation annotation : annotations) {
        if (annotation.getText().matches("@Inject")) {
          return annotation;
        }
      }
    }
    return null;
  }

  static PsiAnnotation findAnnotationOnMethod(PsiMethod psiMethod, String annotationName) {
    PsiModifierList modifierList = psiMethod.getModifierList();
    for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
      if (annotationName.equals(psiAnnotation.getQualifiedName())) {
        return psiAnnotation;
      }
    }
    return null;
  }

  static boolean hasAnnotation(PsiMethod psiMethod, String annotationName) {
    return findAnnotationOnMethod(psiMethod, annotationName) != null;
  }

  static PsiMethod findMethod(PsiElement element) {
    if (element == null) {
      return null;
    } else if (element instanceof PsiMethod) {
      return (PsiMethod) element;
    } else {
      return findMethod(element.getParent());
    }
  }
}
