package com.squareup.idea.dagger

import com.intellij.openapi.components.ProjectComponent

class DaggerProjectComponent : ProjectComponent {
  override fun projectOpened() {
    System.out.println("Hello World!")
  }
}