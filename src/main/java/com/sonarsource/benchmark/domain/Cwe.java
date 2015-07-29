package com.sonarsource.benchmark.domain;

import java.util.ArrayList;
import java.util.List;


public class Cwe {

  int number;
  List<String> ruleKeys = new ArrayList<>();
  List<BenchmarkTest> benchmarkTests = new ArrayList<>();
  List<BenchmarkTest> falsePositives = new ArrayList<>();
  List<BenchmarkTest> falseNegatives = new ArrayList<>();
  List<BenchmarkTest> truePositives = new ArrayList<>();
  List<BenchmarkTest> trueNegatives = new ArrayList<>();
  int issueCount = 0;
  float positiveAccuracy = 0;
  float negativeAccuracy = 0;

  public Cwe(int number) {

    this.number = number;
  }

  public void sortResults() {

    for (BenchmarkTest bt : benchmarkTests) {

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
    positiveAccuracy = ((float)truePositives.size()/expectedPositives) * 100;
    negativeAccuracy = ((float)trueNegatives.size()/expectedNegatives) * 100;

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

  public void setRuleKeys(List<String> ruleKeys) {

    this.ruleKeys = ruleKeys;
  }

  public void addBenchmarkTest(BenchmarkTest bt) {
    benchmarkTests.add(bt);
  }

  public List<BenchmarkTest> getBenchmarkTests() {

    return benchmarkTests;
  }

  public void setBenchmarkTests(List<BenchmarkTest> benchmarkTests) {

    this.benchmarkTests = benchmarkTests;
  }

  public List<BenchmarkTest> getFalsePositives() {

    return falsePositives;
  }

  public List<BenchmarkTest> getFalseNegatives() {

    return falseNegatives;
  }

  public List<BenchmarkTest> getTruePositives() {

    return truePositives;
  }

  public List<BenchmarkTest> getTrueNegatives() {

    return trueNegatives;
  }

  public int getIssueCount() {

    return issueCount;
  }

  public float getPositiveAccuracy() {

    return positiveAccuracy;
  }

  public float getNegativeAccuracy() {

    return negativeAccuracy;
  }

  public int getNumber() {

    return number;
  }
}
