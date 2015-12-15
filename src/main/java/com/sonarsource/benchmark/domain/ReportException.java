/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.domain;

public class ReportException extends RuntimeException {

  public ReportException(String message) {
    super(message);
  }

  public ReportException(Exception e) {
    super(e);
  }
}

