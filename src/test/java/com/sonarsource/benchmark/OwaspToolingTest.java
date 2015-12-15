/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark;

import com.sonarsource.benchmark.service.Director;
import org.junit.Test;


public class OwaspToolingTest {

  @Test
  public void runProcess(){
    if("true".equals(System.getProperty("reports.generation", "false"))) {

      Director director = new Director();
      director.setBuildSnapshot(true);

      director.analyzeBenchmarkAccuracy();
    }
  }
}
