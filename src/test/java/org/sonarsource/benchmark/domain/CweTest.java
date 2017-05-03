/*
 * SonarSource :: OWASP Benchmark Tooling
 * Copyright (C) 2015-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.benchmark.domain;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class CweTest {

  @Test
  public void testSortResults() {

    Cwe cwe = new Cwe(456);
    BenchmarkSample falseNeg = new BenchmarkSample("file0001", true, cwe);
    cwe.addBenchmarkSample(falseNeg);

    BenchmarkSample truePos = new BenchmarkSample("file0002", true, cwe);
    truePos.addIssueRule("S123");
    cwe.addBenchmarkSample(truePos);

    BenchmarkSample falsePos = new BenchmarkSample("file0003", false, cwe);
    falsePos.addIssueRule("S123");
    cwe.addBenchmarkSample(falsePos);

    BenchmarkSample trueNeg = new BenchmarkSample("file0004", false, cwe);
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

  @Test
  public void testMundane() {

    Cwe cwe = new Cwe(111);

    String ruleKey = "S456";
    cwe.addRuleKey(ruleKey);
    assertThat(cwe.getRuleKeys()).hasSize(1);
    assertThat(cwe.getRuleKeys()).contains(ruleKey);
  }

}
