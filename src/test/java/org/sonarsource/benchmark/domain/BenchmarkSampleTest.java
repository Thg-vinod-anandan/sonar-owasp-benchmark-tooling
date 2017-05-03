/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package org.sonarsource.benchmark.domain;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class BenchmarkSampleTest {

  @Test
  public void testBadResult() {

    Cwe cwe = new Cwe(456);

    BenchmarkSample bs = new BenchmarkSample("file0001", true, cwe);
    bs.addIssueRule("S123");

    assertThat(bs.isBadResult()).isFalse();

    bs = new BenchmarkSample("file0002", false, cwe);
    bs.addIssueRule("S123");

    assertThat(bs.isBadResult()).isTrue();
  }

}
