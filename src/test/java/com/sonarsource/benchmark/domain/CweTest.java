/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.domain;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class CweTest {

  @Test
  public void testSortResults() {

    Cwe cwe = new Cwe(456);
    BenchmarkSample falseNeg = new BenchmarkSample("file0001", true);
    cwe.addBenchmarkSample(falseNeg);

    BenchmarkSample truePos = new BenchmarkSample("file0002", true);
    truePos.addIssueRule("S123");
    cwe.addBenchmarkSample(truePos);

    BenchmarkSample falsePos = new BenchmarkSample("file0003", false);
    falsePos.addIssueRule("S123");
    cwe.addBenchmarkSample(falsePos);

    BenchmarkSample trueNeg = new BenchmarkSample("file0004", false);
    cwe.addBenchmarkSample(trueNeg);

    cwe.sortResults();

    assertThat(cwe.getIssueCount()).isEqualTo(2);
    assertThat(cwe.getFalseNegatives()).hasSize(1);
    assertThat(cwe.getTruePositives()).hasSize(1);
    assertThat(cwe.getFalsePositives()).hasSize(1);
    assertThat(cwe.getTrueNegatives()).hasSize(1);
    assertThat(cwe.getPositiveAccuracyPercentage()).isGreaterThanOrEqualTo((float)50.0).isLessThanOrEqualTo((float)51.0);
    assertThat(cwe.getNegativeAccuracyPercentage()).isGreaterThanOrEqualTo((float)50.0).isLessThanOrEqualTo((float)51.0);
  }

}
