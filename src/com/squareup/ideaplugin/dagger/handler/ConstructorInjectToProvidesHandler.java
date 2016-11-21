package com.squareup.ideaplugin.dagger.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.pom.Navigatable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.squareup.ideaplugin.dagger.Decider;
import com.squareup.ideaplugin.dagger.PickTypeAction;
import com.squareup.ideaplugin.dagger.PsiConsultantImpl;
import com.squareup.ideaplugin.dagger.ShowUsagesAction;

import java.awt.event.MouseEvent;

import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_INJECT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.MAX_USAGES;

/**
 * Handles linking from constructor @Inject(ion) to @Provides. If the Constructor takes multiple
 * parameters, a dialog will pop-up asking the user which parameter type they'd like to look at.
 *
 * Aside from that popup, this is exactly like {@link FieldInjectToProvidesHandler}.
 */
public class ConstructorInjectToProvidesHandler implements GutterIconNavigationHandler<PsiElement> {
  @Override public void navigate(final MouseEvent mouseEvent, PsiElement psiElement) {
    if (!(psiElement instanceof PsiMethod)) {
      throw new IllegalStateException("Called with non-method: " + psiElement);
    }

    PsiMethod psiMethod = (PsiMethod) psiElement;
    PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
    if (parameters.length == 1) {
      showUsages(mouseEvent, parameters[0]);
    } else {
      new PickTypeAction().startPickTypes(new RelativePoint(mouseEvent), parameters,
          new PickTypeAction.Callback() {
            @Override public void onParameterChosen(PsiParameter selected) {
              showUsages(mouseEvent, selected);
            }
          });
    }
  }

  private void showUsages(MouseEvent mouseEvent, PsiParameter psiParameter) {
    // Check to see if class type of psiParameter has constructor with @Inject. Otherwise, proceed.
    if (navigateToConstructorIfProvider(psiParameter)) {
      return;
    }
    new ShowUsagesAction(new Decider.ConstructorParameterInjectDecider(psiParameter)).startFindUsages(
        PsiConsultantImpl.checkForLazyOrProvider(psiParameter), new RelativePoint(mouseEvent),
        PsiUtilBase.findEditor(psiParameter), MAX_USAGES);
  }

  private boolean navigateToConstructorIfProvider(PsiParameter psiParameter) {
    PsiTypeElement declaringTypeElement = psiParameter.getTypeElement();
    PsiClass classElement = JavaPsiFacade.getInstance(psiParameter.getProject()).findClass(
            declaringTypeElement.getType().getCanonicalText(),
            declaringTypeElement.getResolveScope());

    if (classElement == null) {
      return false;
    }

    for (PsiMethod method : classElement.getConstructors()) {
      if (PsiConsultantImpl.hasAnnotation(method, CLASS_INJECT) && navigateToElement(method)) {
          return true;
      }
    }
    return false;
  }

  private boolean navigateToElement(PsiElement element) {
    PsiElement navigationElement = element.getNavigationElement();
    if (navigationElement != null && navigationElement instanceof Navigatable &&
            ((Navigatable) navigationElement).canNavigate()) {
      ((Navigatable) navigationElement).navigate(true);
      return true;
    }
    return false;
  }
}
