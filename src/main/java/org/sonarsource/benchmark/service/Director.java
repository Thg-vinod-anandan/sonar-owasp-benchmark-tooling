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

import org.sonarsource.benchmark.domain.Constants;
import org.sonarsource.benchmark.get.Fetcher;
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
