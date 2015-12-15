/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.Cwe;
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
