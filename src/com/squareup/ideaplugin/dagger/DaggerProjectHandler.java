package com.squareup.ideaplugin.dagger;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DaggerProjectHandler extends AbstractProjectComponent {
  public static final String SUBSCRIBE_CLASS_NAME = "com.squareup.otto.Subscribe";
  public static final String PRODUCER_CLASS_NAME = "com.squareup.otto.Produce";
  private static final Key<DaggerProjectHandler> KEY = Key.create(DaggerProjectHandler.class.getName());
  public static final Logger LOGGER = Logger.getInstance(DaggerProjectHandler.class);

  //private final FindUsagesManager findUsagesManager;
  //private final PsiManager psiManager;
  private final Map<VirtualFile, Set<String>> fileToEventClasses = new HashMap<VirtualFile, Set<String>>();
  private final Set<VirtualFile> filesToScan = new HashSet<VirtualFile>();
  private final Set<String> allEventClasses = new HashSet<String>();

  public PsiTreeChangeAdapter listener;

  protected DaggerProjectHandler(Project project, PsiManager psiManager) {
    super(project);
    //this.findUsagesManager =
    //    ((FindManagerImpl) FindManager.getInstance(project)).getFindUsagesManager();
    //this.psiManager = psiManager;
    //project.putUserData(KEY, this);
    System.out.println("DaggerProjectHandler initialized");
  }
}
