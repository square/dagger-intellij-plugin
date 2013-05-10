package com.squareup.ideaplugin.dagger.handler;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.squareup.ideaplugin.dagger.Decider;
import com.squareup.ideaplugin.dagger.PickTypeAction;
import com.squareup.ideaplugin.dagger.PsiConsultantImpl;
import com.squareup.ideaplugin.dagger.ShowUsagesAction;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
    if (!psiMethod.isConstructor()) {
      throw new IllegalStateException("Called with non-constructor: " + psiElement);
    }

    PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
    List<PsiClass> psiClassList = new ArrayList<PsiClass>();
    for (PsiParameter parameter : parameters) {
      PsiClass injectedClass = PsiConsultantImpl.getClass(parameter);
      PsiClass parameterClass = PsiConsultantImpl.checkForLazyOrProvider(parameter, injectedClass);
      psiClassList.add(parameterClass);
    }

    if (psiClassList.size() > 1) {
      new PickTypeAction().startPickTypes(new RelativePoint(mouseEvent), psiClassList,
          new PickTypeAction.Callback() {
            @Override public void onClassChosen(PsiClass clazz) {
              showUsages(mouseEvent, clazz);
            }
          });
    }
  }

  private void showUsages(MouseEvent mouseEvent, PsiClass firstType) {
    new ShowUsagesAction(Decider.PROVIDERS).startFindUsages(firstType,
        new RelativePoint(mouseEvent), PsiUtilBase.findEditor(firstType), MAX_USAGES);
  }
}
