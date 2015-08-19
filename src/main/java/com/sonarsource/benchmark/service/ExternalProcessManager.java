/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonarsource.benchmark.domain.ReportException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;


public class ExternalProcessManager {

  private static final String SONAR_VERSION = "5.1";

  private static final Logger LOGGER = Logger.getLogger(ExternalProcessManager.class.getName());


  private Orchestrator orchestrator = null;


  public void compile(Path targetProject, String command) {

    LOGGER.info(command + "ing project at " + targetProject);

    Properties props = new Properties();
    props.put("skipTests", "true");

    InvocationRequest request = new DefaultInvocationRequest();

    request.setPomFile(targetProject.resolve("pom.xml").toFile());
    request.setGoals(Arrays.asList(command));
    request.setProperties(props);

    Invoker invoker = new DefaultInvoker();
    try {
      invoker.execute(request);
    } catch (MavenInvocationException e) {
      throw new ReportException(e);
    }
  }

  public void analyze(Path targetProject) {

    LOGGER.info("Analzying project at " + targetProject);

    InvocationRequest request = new DefaultInvocationRequest();

    Properties props = new Properties();
    props.put("sonar.host.url", orchestrator.getServer().getUrl());
    props.put("sonar.jdbc.url", orchestrator.getDatabase().getClient().getUrl());
    props.put("sonar.scm.disabled", "true");
    props.put("sonar.cpd.exclusions", "**/*.java");
    props.put("sonar.importSources", "false");
    props.put("sonar.skipDesign", "true");
    props.put("sonar.exclusions", "**/*.xml");

    request.setPomFile(targetProject.resolve("pom.xml").toFile())
            .setGoals(Arrays.asList("sonar:sonar"))
            .setMavenOpts("-Xmx2048m -server")
            .setProperties(props);

    Invoker invoker = new DefaultInvoker();
    try {
      invoker.execute(request);
      new SynchronousAnalyzer(orchestrator.getServer(), 60 * 1000, 1).waitForDone();
    } catch (MavenInvocationException e) {
      throw new ReportException(e);
    }
  }

  public String startOrchestrator(File plugin) {

    LOGGER.info("Starting platform");
    if (orchestrator == null) {
      orchestrator = Orchestrator
              .builderEnv()
              .setServerProperty("sonar.web.javaOpts", "-Xmx2G -Xms1G -XX:MaxPermSize=100m -XX:+HeapDumpOnOutOfMemoryError -server")
              .setOrchestratorProperty("sonar.runtimeVersion", SONAR_VERSION)
              .setOrchestratorProperty("orchestrator.updateCenterUrl",
                      "http://update.sonarsource.org/update-center-dev.properties")
              .setOrchestratorProperty("sonar.jdbc.dialect", "h2")
              .addPlugin(FileLocation.of(plugin))
              .setMainPluginKey("java")

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

  public File getExactFileName(Path path, String pattern) {

    File file = path.toFile();
    if (file.isDirectory()) {
      for (File f : file.listFiles()){
        if (f.getName().matches(pattern)) {
          return f;
        }
      }
    }
    return null;
  }

}
