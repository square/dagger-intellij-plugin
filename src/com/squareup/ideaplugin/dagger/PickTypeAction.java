package com.squareup.ideaplugin.dagger;

import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiParameter;
import com.intellij.ui.awt.RelativePoint;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class PickTypeAction {

  public void startPickTypes(RelativePoint relativePoint, PsiParameter[] psiParameters,
      final Callback callback) {
    if (psiParameters.length == 0) return;

    ListPopup listPopup = JBPopupFactory.getInstance()
        .createListPopup(new BaseListPopupStep<PsiParameter>("Select Type", psiParameters) {
          @NotNull @Override public String getTextFor(PsiParameter value) {
            StringBuilder builder = new StringBuilder();

            Set<String> annotations = PsiConsultantImpl.getQualifierAnnotations(value);
            for (String annotation : annotations) {
              String trimmed = annotation.substring(annotation.lastIndexOf(".") + 1);
              builder.append("@").append(trimmed).append(" ");
            }

            PsiClass notLazyOrProvider = PsiConsultantImpl.checkForLazyOrProvider(value);
            return builder.append(notLazyOrProvider.getName()).toString();
          }

          @Override public PopupStep onChosen(PsiParameter selectedValue, boolean finalChoice) {
            callback.onParameterChosen(selectedValue);
            return super.onChosen(selectedValue, finalChoice);
          }
        });

    listPopup.show(relativePoint);
  }

  public interface Callback {
    void onParameterChosen(PsiParameter clazz);
  }
}
