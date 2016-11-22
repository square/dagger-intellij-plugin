package com.squareup.ideaplugin.dagger;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
    } else if (psiElement instanceof PsiMethod) {
      return ((PsiMethod) psiElement).getContainingClass();
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
    if (psiMethod.isConstructor()) {
      return psiMethod.getContainingClass();
    }

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

    return getPsiClass(wrapperClass, psiFieldType);
  }

  public static PsiClass checkForLazyOrProvider(PsiParameter psiParameter) {
    PsiClass wrapperClass = PsiConsultantImpl.getClass(psiParameter);

    PsiType psiParameterType = psiParameter.getType();
    if (!(psiParameterType instanceof PsiClassType)) {
      return wrapperClass;
    }

    return getPsiClass(wrapperClass, psiParameterType);
  }

  private static PsiClass getPsiClass(PsiClass wrapperClass, PsiType psiFieldType) {
    PsiClassType psiClassType = (PsiClassType) psiFieldType;

    PsiClassType.ClassResolveResult classResolveResult = psiClassType.resolveGenerics();
    PsiClass outerClass = classResolveResult.getElement();

    // If Lazy<Foo> or Provider<Foo>, extract Foo as the interesting type.
    if (PsiConsultantImpl.isLazyOrProvider(outerClass)) {
      PsiType genericType = extractFirstTypeParameter(psiClassType);
      // Convert genericType to its PsiClass and store in psiClass
      wrapperClass = getClass(genericType);
    }

    return wrapperClass;
  }

  public static boolean hasTypeParameters(PsiElement psiElement, List<PsiType> typeParameters) {
    List<PsiType> actualTypeParameters = getTypeParameters(psiElement);
    return actualTypeParameters.equals(typeParameters);
  }

  public static List<PsiType> getTypeParameters(PsiElement psiElement) {
    PsiClassType psiClassType = getPsiClassType(psiElement);
    if (psiClassType == null) {
      return new ArrayList<PsiType>();
    }

    if (PsiConsultantImpl.isLazyOrProvider(getClass(psiClassType))) {
      psiClassType = extractFirstTypeParameter(psiClassType);
    }

    Collection<PsiType> typeParameters =
        psiClassType.resolveGenerics().getSubstitutor().getSubstitutionMap().values();
    return new ArrayList<PsiType>(typeParameters);
  }

  private static PsiClassType getPsiClassType(PsiElement psiElement) {
    if (psiElement instanceof PsiVariable) {
      return (PsiClassType) ((PsiVariable) psiElement).getType();
    } else if (psiElement instanceof PsiMethod) {
      return (PsiClassType) ((PsiMethod) psiElement).getReturnType();
    }
    return null;
  }

  private static boolean isLazyOrProvider(PsiClass psiClass) {
    if (psiClass == null) {
      return false;
    }
    Project project = psiClass.getProject();
    JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
    GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);

    PsiClass lazyClass = javaPsiFacade.findClass(CLASS_LAZY, globalSearchScope);
    PsiClass providerClass = javaPsiFacade.findClass(CLASS_PROVIDER, globalSearchScope);

    return psiClass.equals(lazyClass) || psiClass.equals(providerClass);
  }

  private static PsiClassType extractFirstTypeParameter(PsiClassType psiClassType) {
    return (PsiClassType) psiClassType.resolveGenerics().getSubstitutor()
        .getSubstitutionMap().values().iterator().next();
  }
}
