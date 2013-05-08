package com.squareup.ideaplugin.dagger;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;

public class DaggerProjectHandler extends AbstractProjectComponent {
  protected DaggerProjectHandler(Project project, PsiManager psiManager) {
    super(project);
    System.out.println("DaggerProjectHandler initialized");
  }
}
