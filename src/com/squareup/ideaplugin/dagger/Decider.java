package com.squareup.ideaplugin.dagger;

import com.intellij.usages.Usage;

public interface Decider {
  boolean shouldShow(Usage usage);
}
