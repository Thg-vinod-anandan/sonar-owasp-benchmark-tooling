/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import com.sonarsource.benchmark.domain.ReportException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;


public class ExternalProcessManager {

  private static final String SONAR_VERSION = "5.1";

  private static final Logger LOGGER = Logger.getLogger(ExternalProcessManager.class.getName());


  private Orchestrator orchestrator = null;


  public void compile(Path targetProject) {

    LOGGER.info("Compiling project at " + targetProject);

    InvocationRequest request = new DefaultInvocationRequest();

    request.setPomFile(targetProject.resolve("pom.xml").toFile());
    request.setGoals(Arrays.asList("compile"));

    Invoker invoker = new DefaultInvoker();
    try {
      invoker.execute(request);
    } catch (MavenInvocationException e) {
      throw new ReportException(e);
    }
  }

  public void analyze(Path targetProject) {

    LOGGER.info("Analzying project at " + targetProject);

    SonarRunner build = SonarRunner.create(targetProject.toFile())
            .setEnvironmentVariable("SONAR_RUNNER_OPTS", "-Xmx2048m -server")
            .setProperty("sonar.importSources", "false")
            .setProperty("sonar.skipDesign", "true")
            .setProperty("sonar.scm.disabled", "true")
            .setProperty("sonar.cpd.exclusions", "**/*.java")
            .setProjectKey("project")
            .setProjectName("project")
            .setProjectVersion("1")
            .setSourceEncoding("UTF-8")
            .setSourceDirs("src/main/java")
            .setProperty("sonar.java.binaries", "target/classes");
            // sonar.java.libraries

    orchestrator.executeBuild(build, false);
    // query server every minute, 1=log each query result
    new SynchronousAnalyzer(orchestrator.getServer(), 60 * 1000, 1).waitForDone();

  }

  public String startOrchestrator() {

    LOGGER.info("Starting platform");

    if (orchestrator == null) {
      orchestrator = Orchestrator
              .builderEnv()
              .setServerProperty("sonar.web.javaOpts", "-Xmx2G -Xms1G -XX:MaxPermSize=100m -XX:+HeapDumpOnOutOfMemoryError -server")
              .setOrchestratorProperty("sonar.runtimeVersion", SONAR_VERSION)
              .setOrchestratorProperty("orchestrator.updateCenterUrl",
                      "http://update.sonarsource.org/update-center-dev.properties")
              .setOrchestratorProperty("sonar.jdbc.dialect", "h2")

              .setOrchestratorProperty("javaVersion", "DEV").addPlugin("java")
              .build();

      orchestrator.start();
      LOGGER.info("Platform available at " + orchestrator.getServer().getUrl());
    }
    return orchestrator.getServer().getUrl();
  }

  public void stopOrchestrator() {
    LOGGER.info("Shutting platform down.");

    if (orchestrator != null) {
      orchestrator.stop();
      orchestrator = null;
    }
  }

}
