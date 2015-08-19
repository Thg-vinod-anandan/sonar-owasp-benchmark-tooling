/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark;

import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.get.Fetcher;
import com.sonarsource.benchmark.service.DataMarshaller;
import com.sonarsource.benchmark.service.ExternalProcessManager;
import com.sonarsource.benchmark.service.Reporter;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;


public class OwaspToolingTest {

  @Test
  public void runProcess(){
    if("true".equals(System.getProperty("reports.generation", "false"))) {

      Fetcher fetcher = new Fetcher();
      ExternalProcessManager epm = new ExternalProcessManager();

      Path pluginProject = fetcher.getFilesFromUrl(Constants.JAVA_PLUGIN_GIT_PROJECT + Constants.GITHUB_ZIP_PATH);
      epm.compile(pluginProject, "install");

      File pluginJar = epm.getExactFileName(pluginProject.resolve("sonar-java-plugin/target"), "sonar-java-plugin-.*SNAPSHOT.jar");

      Path benchmarkProject = fetcher.getFilesFromUrl(Constants.BENCHMARK_GIT_PROJECT + Constants.GITHUB_ZIP_PATH);

      DataMarshaller marshal = new DataMarshaller();
      marshal.readBenchmarkTests(benchmarkProject);

      epm.compile(benchmarkProject, "compile");

      String instance = epm.startOrchestrator(pluginJar);

      marshal.activateCweRules(instance);

      epm.analyze(benchmarkProject);

      marshal.addIssuesToBenchmarkTests(instance);

      epm.stopOrchestrator();

      Reporter reporter = new Reporter();
      reporter.generateReports(marshal);
    }
  }
}
