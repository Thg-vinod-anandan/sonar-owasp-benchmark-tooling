/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonarsource.benchmark.domain;

public class Constants {
  public static final String CWE_URL_ROOT = "http://cwe.mitre.org/data/definitions/";
  public static final String BENCHMARK_GIT_PROJECT = "https://github.com/OWASP/Benchmark/";
  public static final String GITHUB_ZIP_PATH = "archive/master.zip";
  public static final String BENCHMARK_TEST_PATH = "tree/master/src/main/java/org/owasp/benchmark/testcode/";
  public static final String JAVA_PLUGIN_GIT_PROJECT = "https://github.com/SonarSource/sonar-java/";

  private Constants(){
    // Nothing to see here. Move along.
  }
}
