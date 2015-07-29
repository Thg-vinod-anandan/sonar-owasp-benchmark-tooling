/*
 * Copyright (C) 2015 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.domain;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkTest {
  String fileName;
  boolean vulnerable;
  String cwe;
  List<String> issueRules = new ArrayList<>();

  public BenchmarkTest(String fileName, boolean vulnerable, String cwe) {

    this.fileName = fileName;
    this.vulnerable = vulnerable;
    this.cwe = cwe;
  }

  public String getFileName() {

    return fileName;
  }

  public boolean isVulnerable() {

    return vulnerable;
  }

  public String getCwe() {

    return cwe;
  }

  public List<String> getIssueRules() {

    return issueRules;
  }

}
