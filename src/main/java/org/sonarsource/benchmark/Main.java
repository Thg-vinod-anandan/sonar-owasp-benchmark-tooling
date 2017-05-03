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
package org.sonarsource.benchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.sonarsource.benchmark.service.Director;


public class Main {

  private Main(){
    // this space intentionally left blank
  }

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
