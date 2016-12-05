package com.squareup.ideaplugin.dagger;

import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import java.util.List;
import java.util.Set;

import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_INJECT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_PROVIDES;
import static com.squareup.ideaplugin.dagger.DaggerConstants.SET_TYPE;

public interface Decider {

  boolean shouldShow(UsageTarget target, Usage usage);

  /** Construct with a PsiMethod from a Provider to find where this is injected. */
  public class ProvidesMethodDecider implements Decider {
    private final PsiClass returnType;
    private final Set<String> qualifierAnnotations;
    private final List<PsiType> typeParameters;

    public ProvidesMethodDecider(PsiMethod psiMethod) {
      this.returnType = PsiConsultantImpl.getReturnClassFromMethod(psiMethod, true);
      this.qualifierAnnotations = PsiConsultantImpl.getQualifierAnnotations(psiMethod);
      this.typeParameters = PsiConsultantImpl.getTypeParameters(psiMethod);
    }

    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

      PsiField field = PsiConsultantImpl.findField(element);
      if (field != null //
          && PsiConsultantImpl.hasAnnotation(field, CLASS_INJECT) //
          && PsiConsultantImpl.hasQuailifierAnnotations(field, qualifierAnnotations)
          && PsiConsultantImpl.hasTypeParameters(field, typeParameters)) {
        return true;
      }

      PsiMethod method = PsiConsultantImpl.findMethod(element);
      if (method != null && (PsiConsultantImpl.hasAnnotation(method, CLASS_INJECT)
              || PsiConsultantImpl.hasAnnotation(method, CLASS_PROVIDES))) {
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
          PsiClass parameterClass = PsiConsultantImpl.checkForLazyOrProvider(parameter);
          if (parameterClass.equals(returnType) && PsiConsultantImpl.hasQuailifierAnnotations(
              parameter, qualifierAnnotations)
              && PsiConsultantImpl.hasTypeParameters(parameter, typeParameters)) {
            return true;
          }
        }
      }

      return false;
    }
  }

  /**
   * Construct with a PsiParameter from an @Inject constructor and then use this to ensure the
   * usage fits.
   */
  public class ConstructorParameterInjectDecider extends IsAProviderDecider {
    public ConstructorParameterInjectDecider(PsiParameter psiParameter) {
      super(psiParameter);
    }
  }

  public class CollectionElementParameterInjectDecider extends IsAProviderDecider {
    public CollectionElementParameterInjectDecider(PsiElement psiParameter) {
      super(psiParameter);
    }

    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiMethod psimethod = PsiConsultantImpl.findMethod(element);

      PsiAnnotationMemberValue attribValue = PsiConsultantImpl
          .findTypeAttributeOfProvidesAnnotation(psimethod);

      // Is it a @Provides method?
      return psimethod != null
          // Ensure it has an @Provides.
          && PsiConsultantImpl.hasAnnotation(psimethod, CLASS_PROVIDES)
          // Check for Qualifier annotations.
          && PsiConsultantImpl.hasQuailifierAnnotations(psimethod, qualifierAnnotations)
          // Right return type.
          && PsiConsultantImpl.getReturnClassFromMethod(psimethod, false)
          .getName()
          .equals(target.getName())
          // Right type parameters.
          && PsiConsultantImpl.hasTypeParameters(psimethod, typeParameters)
          // @Provides(type=SET)
          && attribValue != null
          && attribValue.textMatches(SET_TYPE);
    }
  }

  /**
   * Construct with a PsiField annotated w/ @Inject and then use this to ensure the
   * usage fits.
   */
  public class FieldInjectDecider extends IsAProviderDecider {
    public FieldInjectDecider(PsiField psiField) {
      super(psiField);
    }
  }

  class IsAProviderDecider implements Decider {
    protected final Set<String> qualifierAnnotations;
    protected final List<PsiType> typeParameters;

    public IsAProviderDecider(PsiElement element) {
      this.qualifierAnnotations = PsiConsultantImpl.getQualifierAnnotations(element);
      this.typeParameters = PsiConsultantImpl.getTypeParameters(element);
    }

    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

      PsiMethod psimethod = PsiConsultantImpl.findMethod(element);

      // For constructors annotated w/ @Inject, this is searched first before committing to the usage search.

      // Is it a @Provides method?
      return psimethod != null
          // Ensure it has an @Provides.
          && PsiConsultantImpl.hasAnnotation(psimethod, CLASS_PROVIDES)

          // Check for Qualifier annotations.
          && PsiConsultantImpl.hasQuailifierAnnotations(psimethod, qualifierAnnotations)

          // Right return type.
          && PsiConsultantImpl.getReturnClassFromMethod(psimethod, false)
          .getName()
          .equals(target.getName())

          // Right type parameters.
          && PsiConsultantImpl.hasTypeParameters(psimethod, typeParameters);
    }
  }
}
