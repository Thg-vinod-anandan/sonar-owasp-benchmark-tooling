/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.domain.Cwe;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;


public class ReporterTest {

  private Reporter reporter = new Reporter();
  private TestUtilities tu = new TestUtilities();


  @Test
  public void testGetHtmlStringBuilder(){
    StringBuilder sb = reporter.getHtmlStringBuilder();
    String str = sb.toString();

    assertThat(str.length()).isGreaterThan(0);
    assertThat(str).contains("<style>");
    assertThat(str).contains("<body>");
  }

  @Test
  public void testAddLinkedCweId() {

    Cwe cwe = new Cwe(456);
    StringBuilder sb = new StringBuilder();

    reporter.addLinkedCweId(cwe, sb);

    String str = sb.toString();

    assertThat(str).contains(Constants.CWE_URL_ROOT + cwe.getNumber());
    assertThat(str).contains(cwe.getId());

  }

  @Test
  public void testGenerateBadResultsReport() {

    Cwe cwe = new Cwe(4);

    String report = reporter.generateBadResultsReport(cwe);

    // no links to bad result test files
    assertThat(report).doesNotContain(Constants.BENCHMARK_GIT_PROJECT);

    try {
      cwe = tu.getDataMarshallerWithBenchmarkTestsAndCwes().getCweMap().get("CWE-78");
      cwe.addRuleKey("squid:S2092");

      report = reporter.generateBadResultsReport(cwe);

      assertThat(report).contains(Constants.BENCHMARK_GIT_PROJECT);
    } catch (ParseException e) {
      fail("Unexpected exception thrown");
    }
  }


}
