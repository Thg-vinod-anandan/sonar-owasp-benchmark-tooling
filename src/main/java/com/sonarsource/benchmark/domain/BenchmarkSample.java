/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.domain;

import java.util.ArrayList;
import java.util.List;


public class BenchmarkSample {

  String fileName;
  boolean vulnerable;
  List<String> issueRules = new ArrayList<>();
  boolean badResult = false;


  public BenchmarkSample(String fileName, boolean vulnerable) {

    this.fileName = fileName;
    this.vulnerable = vulnerable;
    if (vulnerable) {
      badResult = true;
    }
  }

  public String getFileName() {

    return fileName;
  }

  public boolean isVulnerable() {

    return vulnerable;
  }

  public List<String> getIssueRules() {

    return issueRules;
  }

  public boolean isBadResult() {
    return badResult;
  }

  public void addIssueRule(String rule) {
    issueRules.add(rule);
    badResult = !vulnerable;
  }

  public int getIssueCount() {
    return issueRules.size();
  }
}
