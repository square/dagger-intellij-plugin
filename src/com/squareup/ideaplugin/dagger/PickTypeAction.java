package com.squareup.ideaplugin.dagger;

import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiClass;
import com.intellij.ui.awt.RelativePoint;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PickTypeAction {

  public void startPickTypes(RelativePoint relativePoint, List<PsiClass> psiClassList,
      final Callback callback) {
    ListPopup listPopup = JBPopupFactory.getInstance()
        .createListPopup(new BaseListPopupStep<PsiClass>("Select Type", psiClassList) {
          @NotNull @Override public String getTextFor(PsiClass value) {
            return value.getName();
          }

          @Override public PopupStep onChosen(PsiClass selectedValue, boolean finalChoice) {
            callback.onClassChosen(selectedValue);
            return super.onChosen(selectedValue, finalChoice);
          }
        });

    listPopup.show(relativePoint);
  }

  public interface Callback {
    void onClassChosen(PsiClass clazz);
  }
}
