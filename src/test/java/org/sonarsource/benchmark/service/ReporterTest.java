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
package org.sonarsource.benchmark.service;

import org.sonarsource.benchmark.domain.Constants;
import org.sonarsource.benchmark.domain.Cwe;
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
