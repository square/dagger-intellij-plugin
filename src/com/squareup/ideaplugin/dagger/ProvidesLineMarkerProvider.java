package com.squareup.ideaplugin.dagger;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiTypeElement;
import com.squareup.ideaplugin.dagger.handler.ConstructorInjectToInjectionPlaceHandler;
import com.squareup.ideaplugin.dagger.handler.ProvidesToInjectHandler;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeHighlighting.Pass.UPDATE_ALL;
import static com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.LEFT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_INJECT;
import static com.squareup.ideaplugin.dagger.DaggerConstants.CLASS_PROVIDES;
import static com.squareup.ideaplugin.dagger.PsiConsultantImpl.hasAnnotation;

public class ProvidesLineMarkerProvider implements LineMarkerProvider {
  private static final Icon ICON = IconLoader.getIcon("/icons/provides.png");

  /**
   * @return a {@link com.intellij.codeInsight.daemon.GutterIconNavigationHandler} if the element
   *         is a PsiMethod annotated with @Provides.
   */
  @Nullable @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    // Check methods first (includes constructors).
    if (element instanceof PsiMethod) {
      PsiMethod methodElement = (PsiMethod) element;

      // Does it have an @Provides?
      if (hasAnnotation(element, CLASS_PROVIDES)) {
        PsiTypeElement returnTypeElement = methodElement.getReturnTypeElement();
        if (returnTypeElement != null) {
          return new LineMarkerInfo<PsiElement>(element, returnTypeElement.getTextRange(), ICON,
              UPDATE_ALL, null, new ProvidesToInjectHandler(), LEFT);
        }
      }

      // Is it an @Inject-able constructor?
      if (methodElement.isConstructor() && hasAnnotation(element, CLASS_INJECT)) {
        return new LineMarkerInfo<PsiElement>(element, element.getTextRange(), ICON,
            UPDATE_ALL, null, new ConstructorInjectToInjectionPlaceHandler(), LEFT);
      }
    }

    return null;
  }

  @Override public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements,
      @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
    // Sure buddy. You ever explain how and we just might.
  }
}
