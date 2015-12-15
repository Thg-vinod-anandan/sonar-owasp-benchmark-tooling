/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.domain;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class BenchmarkSampleTest {

  @Test
  public void testBadResult() {

    BenchmarkSample bs = new BenchmarkSample("file0001", true);
    bs.addIssueRule("S123");

    assertThat(bs.isBadResult()).isFalse();

    bs = new BenchmarkSample("file0002", false);
    bs.addIssueRule("S123");

    assertThat(bs.isBadResult()).isTrue();
  }

}
