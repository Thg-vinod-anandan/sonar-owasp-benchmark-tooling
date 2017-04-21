/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.OrchestratorBuilder;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonarsource.benchmark.domain.ReportException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.fest.util.Strings;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExternalProcessManager {

  private static final String SONAR_VERSION = "5.6";

  private static final Logger LOGGER = Logger.getLogger(ExternalProcessManager.class.getName());


  private Orchestrator orchestrator = null;


  public void compile(Path targetProject, String command) {

    LOGGER.log(Level.INFO, "for project at {0}, running: mvn {1}", new String[]{targetProject.toString(), command});

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

  public void analyze(Path targetProject, String instance) {

    LOGGER.log(Level.INFO,"Analzying project at {0}", targetProject);

    InvocationRequest request = new DefaultInvocationRequest();

    String server = instance;
    if (Strings.isNullOrEmpty(server)){
      server = orchestrator.getServer().getUrl();
    }

    Properties props = new Properties();
    props.put("sonar.host.url", server);

    if (orchestrator != null) {
      props.put("sonar.jdbc.url", orchestrator.getDatabase().getClient().getUrl());
    }
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
      new SynchronousAnalyzer(server, 60 * 1000, 1).waitForDone();
    } catch (MavenInvocationException e) {
      throw new ReportException(e);
    }
  }

  public String startOrchestrator(File plugin) {

    LOGGER.info("Starting platform");
    if (orchestrator == null) {


      OrchestratorBuilder builder = Orchestrator.builderEnv()
              .setServerProperty("sonar.web.javaOpts", "-Xmx2G -Xms1G -XX:MaxPermSize=100m -XX:+HeapDumpOnOutOfMemoryError -server")
              .setOrchestratorProperty("sonar.runtimeVersion", SONAR_VERSION)
              .setOrchestratorProperty("orchestrator.updateCenterUrl",
                      "http://update.sonarsource.org/update-center-dev.properties")
              .setOrchestratorProperty("sonar.jdbc.dialect", "h2");

      if (plugin != null && plugin.exists()) {
        builder.addPlugin(FileLocation.of(plugin))
                .setMainPluginKey("java");
      } else {
        builder.setOrchestratorProperty("javaVersion", "LATEST_RELEASE").addPlugin("java");
      }

      orchestrator = builder.build();

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
