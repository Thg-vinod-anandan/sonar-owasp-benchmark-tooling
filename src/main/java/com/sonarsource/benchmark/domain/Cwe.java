package com.sonarsource.benchmark.domain;

import java.util.ArrayList;
import java.util.List;

public class Cwe {
  String id;
  List<String> ruleKeys = new ArrayList<>();
  List<BenchmarkTest> benchmarkTests = new ArrayList<>();

  public Cwe(String id) {

    this.id = id;
  }

  public String getId() {

    return id;
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
}
