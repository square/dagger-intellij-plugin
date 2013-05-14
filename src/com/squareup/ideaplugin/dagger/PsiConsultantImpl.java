package com.squareup.ideaplugin.dagger;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.HashSet;
import java.util.Set;

import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_LAZY;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_PROVIDER;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_QUALIFIER;

public class PsiConsultantImpl {

  public static boolean hasAnnotation(PsiElement element, String annotationName) {
    return findAnnotation(element, annotationName) != null;
  }

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

  public static Set<String> getQualifierAnnotations(PsiElement element) {
    Set<String> qualifiedClasses = new HashSet<String>();

    if (element instanceof PsiModifierListOwner) {
      PsiModifierListOwner listOwner = (PsiModifierListOwner) element;
      PsiModifierList modifierList = listOwner.getModifierList();

      if (modifierList != null) {
        for (PsiAnnotation psiAnnotation : modifierList.getAnnotations()) {
          if (psiAnnotation != null && psiAnnotation.getQualifiedName() != null) {
            JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(element.getProject());
            PsiClass psiClass = psiFacade.findClass(psiAnnotation.getQualifiedName(),
                GlobalSearchScope.projectScope(element.getProject()));

            if (hasAnnotation(psiClass, CLASS_QUALIFIER)) {
              qualifiedClasses.add(psiAnnotation.getQualifiedName());
            }
          }
        }
      }
    }

    return qualifiedClasses;
  }

  public static boolean hasQuailifierAnnotations(PsiElement element, Set<String> types) {
    Set<String> actualAnnotations = getQualifierAnnotations(element);
    return actualAnnotations.equals(types);
  }

  public static PsiMethod findMethod(PsiElement element) {
    if (element == null) {
      return null;
    } else if (element instanceof PsiMethod) {
      return (PsiMethod) element;
    } else {
      return findMethod(element.getParent());
    }
  }

  public static PsiClass getClass(PsiElement psiElement) {
    if (psiElement instanceof PsiVariable) {
      PsiVariable variable = (PsiVariable) psiElement;
      return getClass(variable.getType());
    }
    return null;
  }

  public static PsiClass getClass(PsiType psiType) {
    if (psiType instanceof PsiClassType) {
      return ((PsiClassType) psiType).resolve();
    }
    return null;
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

  public static PsiClass checkForLazyOrProvider(PsiField psiField) {
    PsiClass wrapperClass = PsiConsultantImpl.getClass(psiField);

    PsiType psiFieldType = psiField.getType();
    if (!(psiFieldType instanceof PsiClassType)) {
      return wrapperClass;
    }

    return getPsiClass(psiField, wrapperClass, psiFieldType);
  }

  public static PsiClass checkForLazyOrProvider(PsiParameter psiParameter) {
    PsiClass wrapperClass = PsiConsultantImpl.getClass(psiParameter);

    PsiType psiParameterType = psiParameter.getType();
    if (!(psiParameterType instanceof PsiClassType)) {
      return wrapperClass;
    }

    return getPsiClass(psiParameter, wrapperClass, psiParameterType);
  }

  private static PsiClass getPsiClass(PsiElement psiElement, PsiClass wrapperClass,
      PsiType psiFieldType) {
    PsiClassType psiClassType = (PsiClassType) psiFieldType;
    Project project = psiElement.getProject();
    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
    GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);

    PsiClass lazyClass = javaPsiFacade.findClass(CLASS_LAZY, globalSearchScope);
    PsiClass providerClass = javaPsiFacade.findClass(CLASS_PROVIDER, globalSearchScope);

    PsiClassType.ClassResolveResult classResolveResult = psiClassType.resolveGenerics();
    PsiClass outerClass = classResolveResult.getElement();

    // If Lazy<Foo> or Provider<Foo>, extract Foo as the interesting type.
    if (outerClass != null //
        && (outerClass.equals(lazyClass) || outerClass.equals(providerClass))) {
      PsiType genericType =
          classResolveResult.getSubstitutor().getSubstitutionMap().values().iterator().next();
      // Convert genericType to its PsiClass and store in psiClass
      wrapperClass = getClass(genericType);
    }

    return wrapperClass;
  }
}
