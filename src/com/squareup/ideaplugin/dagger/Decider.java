package com.squareup.ideaplugin.dagger;

import com.intellij.usages.Usage;

public interface Decider {
  Decider ALL = new Decider() {
    @Override public boolean shouldShow(Usage usage) {
      return true;
    }
  };

  boolean shouldShow(Usage usage);
}
