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
package org.sonarsource.benchmark.domain;

import java.util.ArrayList;
import java.util.List;


public class BenchmarkSample {

  String fileName;
  boolean vulnerable;
  List<String> issueRules = new ArrayList<>();
  boolean badResult = false;
  Cwe relatedCwe;


  public BenchmarkSample(String fileName, boolean vulnerable, Cwe relatedCwe) {

    this.fileName = fileName;
    this.relatedCwe = relatedCwe;
    this.vulnerable = vulnerable;
    if (vulnerable) {
      badResult = true;
    }
  }

  public String getFileName() {

    return fileName;
  }

  public boolean isVulnerable() {

    return vulnerable;
  }

  public List<String> getIssueRules() {

    return issueRules;
  }

  public boolean isBadResult() {
    return badResult;
  }

  public void addIssueRule(String rule) {
    issueRules.add(rule);
    badResult = !vulnerable;
  }

  public int getIssueCount() {
    return issueRules.size();
  }


  public Cwe getRelatedCwe() {
    return relatedCwe;
  }
}
