package com.squareup.ideaplugin.dagger.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.squareup.ideaplugin.dagger.Decider;
import com.squareup.ideaplugin.dagger.PsiConsultantImpl;
import com.squareup.ideaplugin.dagger.ShowUsagesAction;
import java.awt.event.MouseEvent;

import static com.squareup.ideaplugin.dagger.DaggerConstants.MAX_USAGES;

/**
 * Handles linking from field @Inject(ion) to @Provides.
 *
 * Ensures that a <code>Lazy<T></code> and <code>Provider<T></code> resolve to the appropriate
 * classes.
 */
public class FieldInjectToProvidesHandler implements GutterIconNavigationHandler<PsiElement> {
  @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
    if (!(psiElement instanceof PsiField)) {
      throw new IllegalStateException("Called with non-field element: " + psiElement);
    }

    PsiField psiField = (PsiField) psiElement;
    PsiClass injectedClass = PsiConsultantImpl.checkForLazyOrProvider(psiField);

    new ShowUsagesAction(new Decider.FieldInjectDecider(psiField)).startFindUsages(injectedClass,
        new RelativePoint(mouseEvent), PsiUtilBase.findEditor(injectedClass), MAX_USAGES);
  }
}
