package com.squareup.ideaplugin.dagger;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;

public class PsiConsultantImpl {

  static PsiAnnotation findAnnotation(PsiElement element, String annotationName) {
    if (element instanceof PsiModifierListOwner) {
      PsiModifierListOwner listOwner = (PsiModifierListOwner) element;
      PsiModifierList modifierList = listOwner.getModifierList();
      if (modifierList != null) {
        for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
          if (annotationName.equals(psiAnnotation.getQualifiedName())) {
            return psiAnnotation;
          }
        }
      }
    }
    return null;
  }

  static boolean hasAnnotation(PsiElement element, String annotationName) {
    return findAnnotation(element, annotationName) != null;
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

  public static PsiClass getClassFromField(PsiField psiField) {
    return ((PsiClassType) psiField.getType()).resolve();
  }

  public static PsiClass getReturnClassFromMethod(PsiMethod psiMethod) {
    PsiClassType returnType = ((PsiClassType) psiMethod.getReturnType());
    if (returnType != null) {
      return returnType.resolve();
    }
    return null;
  }

  public static PsiField findField(PsiElement element) {
    if (element == null) {
      return null;
    } else if (element instanceof PsiField) {
      return (PsiField) element;
    } else {
      return findField(element.getParent());
    }
  }
}
