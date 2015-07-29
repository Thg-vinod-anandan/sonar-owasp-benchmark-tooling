/*
 * Copyright (C) 2015 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark;

import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.get.Fetcher;
import com.sonarsource.benchmark.service.DataMarshaller;
import com.sonarsource.benchmark.service.Reporter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  private Main() {
    // utility class private constructor
  }

  public static void main(String [] args) {

    Fetcher fetcher = new Fetcher();

//    Path path = fetcher.getFilesFromUrl(Constants.BENCHMARK_GIT_PROJECT + Constants.BENCHMARK_ZIP_PATH);
Path path = Paths.get("/home/ganncamp/workspace/sonar-owasp-benchmark-tooling/target/Benchmark-master");
    DataMarshaller marshal = new DataMarshaller();
    marshal.readBenchmarkTests(path);

    marshal.activateCweRules("http://localhost:9000");

    // compile benchmark project
    // run analysis
//    MavenCli cli = new MavenCli();
//    cli.doMain(new String[]{"clean", "install"}, "project_dir", System.out, System.out);

    marshal.addIssuesToBenchmarkTests("http://localhost:9000");

    Reporter reporter = new Reporter();
    reporter.generateReports(marshal);

  }

}
