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

import java.nio.file.Path;


public class Main {

  private Main() {
    // utility class private constructor
  }

  public static void main(String [] args) {

    Fetcher fetcher = new Fetcher();
    Path path = fetcher.getFilesFromUrl(Constants.BENCHMARK_GIT_PROJECT + Constants.BENCHMARK_ZIP_PATH);

    DataMarshaller marshal = new DataMarshaller();
    marshal.readBenchmarkTests(path);

    ExternalProcessManager epm = new ExternalProcessManager();
    epm.compile(path);

    String instance = epm.startOrchestrator();

    marshal.activateCweRules(instance);

    epm.analyze(path);

    marshal.addIssuesToBenchmarkTests(instance);

    epm.stopOrchestrator();

    Reporter reporter = new Reporter();
    reporter.generateReports(marshal);

  }

}
