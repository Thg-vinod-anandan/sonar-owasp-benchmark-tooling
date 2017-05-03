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

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class BenchmarkSampleTest {

  @Test
  public void testBadResult() {

    Cwe cwe = new Cwe(456);

    BenchmarkSample bs = new BenchmarkSample("file0001", true, cwe);
    bs.addIssueRule("S123");

    assertThat(bs.isBadResult()).isFalse();

    bs = new BenchmarkSample("file0002", false, cwe);
    bs.addIssueRule("S123");

    assertThat(bs.isBadResult()).isTrue();
  }

}
