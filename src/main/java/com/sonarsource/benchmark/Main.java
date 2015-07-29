/*
 * Copyright (C) 2015 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark;

import com.sonarsource.benchmark.get.Fetcher;
import com.sonarsource.benchmark.issues.DataGatherer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  private Main() {
    // utility class private constructor
  }

  public static void main(String [] args) {

    Fetcher fetcher = new Fetcher();

//    Path path = fetcher.getFilesFromUrl("https://github.com/OWASP/Benchmark/archive/master.zip");
Path path = Paths.get("/home/ganncamp/workspace/sonar-owasp-benchmark-tooling/target/Benchmark-master");
    DataGatherer retriever = new DataGatherer();
    retriever.readBenchmarkTests(path);

    retriever.activateCweRules("http://localhost:9000");

    // compile benchmark project
    // run analysis
//    MavenCli cli = new MavenCli();
//    cli.doMain(new String[]{"clean", "install"}, "project_dir", System.out, System.out);

    retriever.addIssuesToBenchmarkTests("http://localhost:9000");

    System.out.println("yo");
  }

}
