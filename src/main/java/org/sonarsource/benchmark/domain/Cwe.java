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

import java.util.ArrayList;
import java.util.List;


public class Cwe {

  private int number;
  private List<String> ruleKeys = new ArrayList<>();
  private List<BenchmarkSample> benchmarkSamples = new ArrayList<>();
  private List<BenchmarkSample> falsePositives = new ArrayList<>();
  private List<BenchmarkSample> falseNegatives = new ArrayList<>();
  private List<BenchmarkSample> truePositives = new ArrayList<>();
  private List<BenchmarkSample> trueNegatives = new ArrayList<>();
  private List<BenchmarkSample> unexpectedIssues = new ArrayList<>();
  private int issueCount = 0;
  private float positiveAccuracyPercentage = 0;
  private float negativeAccuracyPercentage = 0;

  public Cwe(int number) {

    this.number = number;
  }

  public void sortResults() {

    for (BenchmarkSample bt : benchmarkSamples) {

      issueCount += bt.getIssueCount();

      if (bt.isBadResult()) {
        if (bt.isVulnerable()) {
          falseNegatives.add(bt);
        } else {
          falsePositives.add(bt);
        }
      } else {
        if (bt.isVulnerable()) {
          truePositives.add(bt);
        } else {
          trueNegatives.add(bt);
        }
      }
    }

    int expectedPositives = truePositives.size() + falseNegatives.size();
    int expectedNegatives = trueNegatives.size() + falsePositives.size();
    positiveAccuracyPercentage = ((float)truePositives.size()/expectedPositives) * 100;
    negativeAccuracyPercentage = ((float)falsePositives.size()/expectedNegatives) * 100;
  }


  public String getId() {

    return "CWE-"+number;
  }

  public void addRuleKey(String ruleKey) {
    ruleKeys.add(ruleKey);
  }

  public List<String> getRuleKeys() {

    return ruleKeys;
  }

  public void addBenchmarkSample(BenchmarkSample bt) {
    benchmarkSamples.add(bt);
  }

  public void addUnexpectedIssue(BenchmarkSample bt) {
    unexpectedIssues.add(bt);
  }

  public List<BenchmarkSample> getUnexpectedIssues(){
    return unexpectedIssues;
  }

  public List<BenchmarkSample> getFalsePositives() {

    return falsePositives;
  }

  public List<BenchmarkSample> getFalseNegatives() {

    return falseNegatives;
  }

  public List<BenchmarkSample> getTruePositives() {

    return truePositives;
  }

  public List<BenchmarkSample> getTrueNegatives() {

    return trueNegatives;
  }

  public int getIssueCount() {

    return issueCount;
  }

  public float getPositiveAccuracyPercentage() {

    return positiveAccuracyPercentage;
  }

  public float getNegativeAccuracyPercentage() {

    return negativeAccuracyPercentage;
  }

  public int getNumber() {

    return number;
  }

  public List<BenchmarkSample> getBenchmarkSamples() {

    return benchmarkSamples;
  }
}
