package com.squareup.ideaplugin.dagger;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DaggerLineMarkerProvider implements LineMarkerProvider {
  public static final Icon ICON = IconLoader.getIcon("/icons/dagger-small.png");
  public static final String PROVIDES_CLASS_NAME = "dagger.Provides";
  private static final String INJECT_CLASS_NAME = "javax.inject.Inject";
  private static final int MAX_USAGES = 100;

  private static final Decider PROVIDERS = new Decider() {
    @Override public boolean shouldShow(Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiMethod psimethod = PsiConsultantImpl.findMethod(element);
      if (psimethod != null && PsiConsultantImpl.hasAnnotation(psimethod, PROVIDES_CLASS_NAME)) {
        return true;
      }
      return false;
    }
  };
  private static final Decider INJECT_FIELDS = new Decider() {
    @Override public boolean shouldShow(Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiField field = PsiConsultantImpl.findField(element);
      if (field != null && PsiConsultantImpl.hasAnnotation(field, INJECT_CLASS_NAME)) {
        return true;
      }
      return false;
    }
  };
  private static final GutterIconNavigationHandler<PsiElement> SHOW_INJECTORS =
      new GutterIconNavigationHandler<PsiElement>() {
        @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
          // @Provides -> @Inject
          if (psiElement instanceof PsiMethod) {
            PsiClass psiClass = PsiConsultantImpl.getReturnClassFromMethod((PsiMethod) psiElement);
            new ShowUsagesAction(INJECT_FIELDS).startFindUsages(psiClass,
                new RelativePoint(mouseEvent), PsiUtilBase.findEditor(psiClass), MAX_USAGES);
          }
        }
      };
  private static final GutterIconNavigationHandler<PsiElement> SHOW_PROVIDERS =
      new GutterIconNavigationHandler<PsiElement>() {
        @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
          // @Inject -> @Provides
          if (psiElement instanceof PsiField) {
            PsiClass psiClass = PsiConsultantImpl.getClassFromField((PsiField) psiElement);
            new ShowUsagesAction(PROVIDERS).startFindUsages(psiClass, new RelativePoint(mouseEvent),
                PsiUtilBase.findEditor(psiClass), MAX_USAGES);
          }
        }
      };

  @Nullable @Override public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    if (element instanceof PsiMethod) { // @Provides
      if (PsiConsultantImpl.hasAnnotation(element, PROVIDES_CLASS_NAME)) {
        return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), ICON,
            Pass.UPDATE_ALL, null, SHOW_INJECTORS, GutterIconRenderer.Alignment.LEFT);
      }
    } else if (element instanceof PsiField) { // @Inject
      if (PsiConsultantImpl.hasAnnotation(element, INJECT_CLASS_NAME)) {
        return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), ICON,
            Pass.UPDATE_ALL, null, SHOW_PROVIDERS, GutterIconRenderer.Alignment.LEFT);
      }
    }

    return null;
  }

  @Override public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements,
      @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
  }
}
