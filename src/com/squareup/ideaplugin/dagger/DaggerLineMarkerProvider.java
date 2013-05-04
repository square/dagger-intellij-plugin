package com.squareup.ideaplugin.dagger;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiTypeElement;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DaggerLineMarkerProvider implements LineMarkerProvider {
  public static final Icon ICON = IconLoader.getIcon("/icons/otto.png");


  @Nullable @Override public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    if (element instanceof PsiMethod) {

    }

    return null;
  }

  public static @Nullable PsiTypeElement getMethodParameter(PsiMethod subscribeMethod) {
    PsiParameterList parameterList = subscribeMethod.getParameterList();
    if (parameterList.getParametersCount() != 1) {
      System.out.println("DEBUG: wha?");
      return null;
    } else {
      PsiParameter subscribeMethodParam = parameterList.getParameters()[0];
      return subscribeMethodParam.getTypeElement();
    }
  }

  @Override public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements,
      @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
  }
}
