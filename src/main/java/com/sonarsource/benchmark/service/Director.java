/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.get.Fetcher;
import org.fest.util.Strings;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class Director {

  protected Path pathToBenchmarkProject;

  private static final Logger LOGGER = Logger.getLogger(Director.class.getName());

  private Fetcher fetcher = new Fetcher();
  private ExternalProcessManager epm = new ExternalProcessManager();

  private String instance;
  private boolean buildSnapshot = false;

  public void analyzeBenchmarkAccuracy() {

    if (pathToBenchmarkProject == null || !pathToBenchmarkProject.toFile().exists()) {
      LOGGER.info("Fetching Benchmark Project");
      pathToBenchmarkProject = fetcher.getFilesFromUrl(Constants.BENCHMARK_GIT_PROJECT + Constants.GITHUB_ZIP_PATH);
      epm.compile(pathToBenchmarkProject, "compile");
    }

    DataMarshaller marshal = new DataMarshaller();
    marshal.readBenchmarkTests(pathToBenchmarkProject);

    if (Strings.isNullOrEmpty(instance)) {
      File pluginJar = null;

      if (buildSnapshot) {
        LOGGER.info("Building latest Java plugin snapshot");
        Path pluginProject = fetcher.getFilesFromUrl(Constants.JAVA_PLUGIN_GIT_PROJECT + Constants.GITHUB_ZIP_PATH);
        epm.compile(pluginProject, "install -Dmaven.test.skip=true -DskipTests=true -DargLine=\"-Xss128m\"");

        pluginJar = epm.getExactFileName(pluginProject.resolve("sonar-java-plugin/target"), "sonar-java-plugin-.*SNAPSHOT.jar");
      }

      LOGGER.info("Starting SonarQube server");
      instance = epm.startOrchestrator(pluginJar);
      marshal.activateCweRules(instance);
    }

    epm.analyze(pathToBenchmarkProject, instance);

    marshal.addIssuesToBenchmarkTests(instance);

    epm.stopOrchestrator();

    Reporter reporter = new Reporter();
    reporter.generateReports(marshal);
  }

  public void setPathToBenchmarkProject(String pathToBenchmarkProject) {
    if (! Strings.isNullOrEmpty(pathToBenchmarkProject)) {
      this.pathToBenchmarkProject = Paths.get(pathToBenchmarkProject);
    }
  }

  public void setInstance(String instance) {

    this.instance = instance;
  }

  public void setBuildSnapshot(boolean buildSnapshot) {

    this.buildSnapshot = buildSnapshot;
  }

}
