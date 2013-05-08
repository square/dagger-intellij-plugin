package com.squareup.ideaplugin.dagger;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeHighlighting.Pass.UPDATE_ALL;
import static com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.LEFT;

public class DaggerLineMarkerProvider implements LineMarkerProvider {
  private static final Icon ICON = IconLoader.getIcon("/icons/dagger-small.png");
  private static final String CLASS_PROVIDES = "dagger.Provides";
  private static final String CLASS_LAZY = "dagger.Lazy";
  private static final String CLASS_PROVIDER = "javax.inject.Provider";
  private static final String CLASS_INJECT = "javax.inject.Inject";
  private static final int MAX_USAGES = 100;

  private static final Decider PROVIDERS = new Decider() {
    @Override public boolean shouldShow(Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiMethod psimethod = PsiConsultantImpl.findMethod(element);
      return psimethod != null && PsiConsultantImpl.hasAnnotation(psimethod, CLASS_PROVIDES);
    }
  };
  private static final Decider INJECTORS = new Decider() {
    @Override public boolean shouldShow(Usage usage) {
      PsiElement element = ((UsageInfo2UsageAdapter) usage).getElement();
      PsiField field = PsiConsultantImpl.findField(element);
      if (field != null && PsiConsultantImpl.hasAnnotation(field, CLASS_INJECT)) {
        return true;
      }

      PsiMethod method = PsiConsultantImpl.findMethod(element);
      return method != null && PsiConsultantImpl.hasAnnotation(method, CLASS_INJECT);
    }
  };

  // @Inject Foo(Bar bar) --> Type list
  private static final GutterIconNavigationHandler<PsiElement> NAV_HANDLER_CTOR_INJECT_LIST =
      new GutterIconNavigationHandler<PsiElement>() {
        @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
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
            psiClassList.add(PsiConsultantImpl.getClass(parameter));
          }

          PsiClass firstType = psiClassList.get(0);
          //TODO(kiran): figure out how to use multiple classes in find usages
          new ShowUsagesAction(PROVIDERS).startFindUsages(firstType, new RelativePoint(mouseEvent),
              PsiUtilBase.findEditor(firstType), MAX_USAGES);
        }
      };

  // @Provides --> @Inject
  private static final GutterIconNavigationHandler<PsiElement> NAV_HANDLER_PROVIDES_TO_INJECT =
      new GutterIconNavigationHandler<PsiElement>() {
        @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
          if (!(psiElement instanceof PsiMethod)) {
            throw new IllegalStateException("Called with non-method: " + psiElement);
          }
          PsiClass psiClass = PsiConsultantImpl.getReturnClassFromMethod((PsiMethod) psiElement);
          new ShowUsagesAction(INJECTORS).startFindUsages(psiClass, new RelativePoint(mouseEvent),
              PsiUtilBase.findEditor(psiClass), MAX_USAGES);
        }
      };

  // @Inject --> @Provides
  private static final GutterIconNavigationHandler<PsiElement> NAV_HANDLER_INJECT_TO_PROVIDES =
      new GutterIconNavigationHandler<PsiElement>() {
        @Override public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
          if (!(psiElement instanceof PsiField)) {
            throw new IllegalStateException("Called with non-field element: " + psiElement);
          }
          PsiField psiField = (PsiField) psiElement;

          PsiType psiFieldType = psiField.getType();
          PsiClass psiClass = PsiConsultantImpl.getClass(psiField);

          if (psiFieldType instanceof PsiClassType) {
            PsiClassType psiClassType = (PsiClassType) psiFieldType;

            Project project = psiElement.getProject();
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            GlobalSearchScope globalSearchScope = GlobalSearchScope.allScope(project);

            PsiClass lazyClass = javaPsiFacade.findClass(CLASS_LAZY, globalSearchScope);
            PsiClass providerClass = javaPsiFacade.findClass(CLASS_PROVIDER, globalSearchScope);

            PsiClassType.ClassResolveResult classResolveResult = psiClassType.resolveGenerics();
            PsiClass outerClass = classResolveResult.getElement();

            // If Lazy<Foo> or Provider<Foo>, extract Foo as the interesting type.
            if (outerClass.equals(lazyClass) || outerClass.equals(providerClass)) {
              PsiType genericType = classResolveResult.getSubstitutor()
                  .getSubstitutionMap()
                  .values()
                  .iterator()
                  .next();
              // TODO convert genericType to its PsiClass and store in psiClass
            }
          }

          new ShowUsagesAction(PROVIDERS).startFindUsages(psiClass, new RelativePoint(mouseEvent),
              PsiUtilBase.findEditor(psiClass), MAX_USAGES);
        }
      };

  @Nullable @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    if (element instanceof PsiMethod) {
      PsiMethod methodElement = (PsiMethod) element;

      // @Provides
      if (PsiConsultantImpl.hasAnnotation(element, CLASS_PROVIDES)) {
        PsiTypeElement returnTypeElement = methodElement.getReturnTypeElement();
        if (returnTypeElement != null) {
          return new LineMarkerInfo<PsiElement>(element, returnTypeElement.getTextRange(), ICON,
              UPDATE_ALL, null, NAV_HANDLER_PROVIDES_TO_INJECT, LEFT);
        }
      }
      // Constructor injection.
      if (methodElement.isConstructor() && PsiConsultantImpl.hasAnnotation(element, CLASS_INJECT)) {
        PsiIdentifier nameIdentifier = methodElement.getNameIdentifier();
        if (nameIdentifier != null) {
          return new LineMarkerInfo<PsiElement>(element, nameIdentifier.getTextRange(), ICON,
              UPDATE_ALL, null, NAV_HANDLER_CTOR_INJECT_LIST, LEFT);
        }
      }
    } else if (element instanceof PsiField) {
      PsiField fieldElement = (PsiField) element;
      PsiTypeElement typeElement = fieldElement.getTypeElement();

      // Field injection.
      if (PsiConsultantImpl.hasAnnotation(element, CLASS_INJECT) && typeElement != null) {
        return new LineMarkerInfo<PsiElement>(element, typeElement.getTextRange(), ICON, UPDATE_ALL,
            null, NAV_HANDLER_INJECT_TO_PROVIDES, LEFT);
      }
    }

    return null;
  }

  @Override public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements,
      @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
  }
}
