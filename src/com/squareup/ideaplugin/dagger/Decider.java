package com.squareup.ideaplugin.dagger;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import java.util.Set;

import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_INJECT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_PROVIDES;

public interface Decider {

  boolean shouldShow(UsageTarget target, Usage usage);

  /** Construct with a PsiMethod from a Provider to find where this is injected. */
  public class ProvidesMethodDecider implements Decider {
    private final PsiClass returnType;
    private final Set<String> qualifierAnnotations;

    public ProvidesMethodDecider(PsiMethod psiMethod) {
      this.returnType = PsiConsultantImpl.getReturnClassFromMethod(psiMethod);
      this.qualifierAnnotations = PsiConsultantImpl.getQualifierAnnotations(psiMethod);
    }

    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();

      PsiField field = PsiConsultantImpl.findField(element);
      if (field != null //
          && PsiConsultantImpl.hasAnnotation(field, CLASS_INJECT) //
          && PsiConsultantImpl.hasQuailifierAnnotations(field, qualifierAnnotations)) {
        return true;
      }

      PsiMethod method = PsiConsultantImpl.findMethod(element);
      if (method != null && PsiConsultantImpl.hasAnnotation(method, CLASS_INJECT)) {
        PsiParameter[] parameters = method.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
          PsiClass parameterClass = PsiConsultantImpl.checkForLazyOrProvider(parameter);
          if (parameterClass.equals(returnType) && PsiConsultantImpl.hasQuailifierAnnotations(
              parameter, qualifierAnnotations)) {
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
      super(PsiConsultantImpl.getQualifierAnnotations(psiParameter));
    }
  }

  /**
   * Construct with a PsiField annotated w/ @Inject and then use this to ensure the
   * usage fits.
   */
  public class FieldInjectDecider extends IsAProviderDecider {
    public FieldInjectDecider(PsiField psiField) {
      super(PsiConsultantImpl.getQualifierAnnotations(psiField));
    }
  }

  class IsAProviderDecider implements Decider {
    private final Set<String> qualifierAnnotations;

    public IsAProviderDecider(Set<String> qualifierAnnotations) {
      this.qualifierAnnotations = qualifierAnnotations;
    }

    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiMethod psimethod = PsiConsultantImpl.findMethod(element);

      return psimethod != null
          // Ensure it has an @Provides.
          && PsiConsultantImpl.hasAnnotation(psimethod, CLASS_PROVIDES)

          // Check for Qualifier annotations.
          && PsiConsultantImpl.hasQuailifierAnnotations(psimethod, qualifierAnnotations)

          // Right return type.
          && PsiConsultantImpl.getReturnClassFromMethod(psimethod)
          .getName()
          .equals(target.getName());
    }
  }
}
