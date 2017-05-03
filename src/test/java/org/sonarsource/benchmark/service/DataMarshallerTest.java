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

import org.sonarsource.benchmark.domain.Cwe;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;


public class DataMarshallerTest {


  TestUtilities tu = new TestUtilities();

  @Test
  public void testBenchmarkTestReading() {
    DataMarshaller dm = tu.getDataMarshallerWithBenchmarkTests();

    assertThat(dm.getCweMap()).hasSize(11);
    assertThat(dm.getCweMap().get("CWE-22").getBenchmarkSamples().size()).isEqualTo(2630);
  }

  @Test
  public void testHandleIssues() {

    try {

      DataMarshaller dm = tu.getDataMarshallerWithBenchmarkTestsAndCwes();

      Map<String, Cwe> map = dm.getCweMap();
      Cwe cwe78 = map.get("CWE-78");

      assertThat(cwe78.getFalsePositives()).hasSize(2);
      assertThat(cwe78.getFalseNegatives()).hasSize(1800);
      assertThat(cwe78.getTruePositives()).hasSize(2);
      assertThat(cwe78.getTrueNegatives()).hasSize(904);

    } catch (ParseException e) {
      fail("Unexpected exception thwon");
    }

  }

}
