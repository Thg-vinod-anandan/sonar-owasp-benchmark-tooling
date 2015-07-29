package com.sonarsource.benchmark.domain;

public class RuleException extends RuntimeException {

  public RuleException(String message) {
    super(message);
  }

  public RuleException(Exception e) {
    super(e);
  }
}

