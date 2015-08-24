/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class DirectorTest {

  @Test
  public void testSetPathToBenchmarkProject() {
    Director director = new Director();
    director.setPathToBenchmarkProject("foo/blah");
    assertThat(director.pathToBenchmarkProject).isNotNull();
  }

}
