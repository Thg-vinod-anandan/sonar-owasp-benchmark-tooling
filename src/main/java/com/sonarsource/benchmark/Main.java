/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sonarsource.benchmark.service.Director;


public class Main {

  public static void main(String [] args) {

    Settings settings = new Settings();
    new JCommander(settings, args);

    if (settings.help) {
      printHelpMessage();
      return;
    }

    Director director = new Director();
    director.setInstance(settings.instance);
    director.setPathToBenchmarkProject(settings.pathToBenchmark);
    director.setBuildSnapshot(settings.latestJava);

    director.analyzeBenchmarkAccuracy();
  }

  private static void printHelpMessage() {

    StringBuilder sb = new StringBuilder();
    sb.append("This application requires M2_HOME to be set in the environment.\n")
            .append("By default it will:\n")
            .append(" 1. download the OWASP Benchmark project sources from Github and compile them\n")
            .append(" 2. download the latest release of the SonarQube Java plugin\n")
            .append(" 3. spin up a SonarQube instance and tune the default Java rule profile\n")
            .append(" 4. analyze the OWASP Benchmark project and output reports\n\n")
            .append("Specify the following parameters to alter that behavior:\n")
            .append(" -benchmarkPath [path]  to skip step 1\n")
            .append(" -instance [url]  to skip steps 2, 3\n")
            .append(" -latestJava  to build and use the latest Java plugin snapshot. Ignored if -instance is set.\n");
    System.out.println(sb.toString());
  }

  public static class Settings{
    @Parameter(names = "--help", help = true)
    private boolean help;

    @Parameter(names = "-instance")
    private String instance = null;

    @Parameter(names="-latestJava")
    private boolean latestJava = false;

    @Parameter(names="-benchmarkPath")
    private String pathToBenchmark;
  }

}
