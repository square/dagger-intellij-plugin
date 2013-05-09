package com.squareup.ideaplugin.dagger;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;

public interface Decider {
  public Decider ALL = new Decider() {
    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      return true;
    }
  };

  public Decider PROVIDERS = new Decider() {
    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiMethod psimethod = PsiConsultantImpl.findMethod(element);

      return psimethod != null
          && PsiConsultantImpl.hasAnnotation(psimethod, DaggerLineMarkerProvider.CLASS_PROVIDES)
          && PsiConsultantImpl.getReturnClassFromMethod(psimethod).getName().equals(target.getName());
    }
  };

  public Decider INJECTORS = new Decider() {
    @Override public boolean shouldShow(UsageTarget target, Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiField field = PsiConsultantImpl.findField(element);
      if (field != null && PsiConsultantImpl.hasAnnotation(field, DaggerLineMarkerProvider.CLASS_INJECT)) {
        return true;
      }

      PsiMethod method = PsiConsultantImpl.findMethod(element);
      return method != null && PsiConsultantImpl.hasAnnotation(method, DaggerLineMarkerProvider.CLASS_INJECT);
    }
  };

  boolean shouldShow(UsageTarget target, Usage usage);

}
