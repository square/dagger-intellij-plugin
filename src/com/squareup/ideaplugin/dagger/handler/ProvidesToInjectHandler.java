package com.squareup.ideaplugin.dagger.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.squareup.ideaplugin.dagger.Decider;
import com.squareup.ideaplugin.dagger.PsiConsultantImpl;
import com.squareup.ideaplugin.dagger.ShowUsagesAction;
import java.awt.event.MouseEvent;

import static com.squareup.ideaplugin.dagger.DaggerConstants.MAX_USAGES;

public class ProvidesToInjectHandler implements GutterIconNavigationHandler<PsiElement> {
  @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
    if (!(psiElement instanceof PsiMethod)) {
      throw new IllegalStateException("Called with non-method: " + psiElement);
    }

    PsiMethod psiMethod = (PsiMethod) psiElement;
    PsiClass psiClass = PsiConsultantImpl.getReturnClassFromMethod(psiMethod, true);

    new ShowUsagesAction(new Decider.ProvidesMethodDecider(psiMethod)).startFindUsages(psiClass,
        new RelativePoint(mouseEvent), PsiUtilBase.findEditor(psiClass), MAX_USAGES);
  }
}
