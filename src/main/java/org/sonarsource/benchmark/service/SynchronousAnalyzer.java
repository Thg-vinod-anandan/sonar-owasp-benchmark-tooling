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


import com.google.common.util.concurrent.Uninterruptibles;
import com.sonar.orchestrator.container.Server;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.wsclient.SonarClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class SynchronousAnalyzer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SynchronousAnalyzer.class);
  static final String RELATIVE_URL = "/api/analysis_reports/is_queue_empty";

  private final Server server;
  private final long delayMs;
  private final int logFrequency;
  private final String instance;
  public static final String ADMIN_LOGIN = "admin";
  public static final String ADMIN_PASSWORD = "admin";
  private SonarClient adminSonarClient;


  public SynchronousAnalyzer(Server server) {
    // check every 1s and log every 10 seconds
    this(server, 1000L, 10);
  }

  public SynchronousAnalyzer(String instance, long delayMs, int logFrequency) {
    this.server = null;
    this.instance = instance;
    this.delayMs = delayMs;
    this.logFrequency = logFrequency;
  }

  public SynchronousAnalyzer(Server server, long delayMs, int logFrequency) {
    this.server = server;
    this.instance = server.getUrl();
    this.delayMs = delayMs;
    this.logFrequency = logFrequency;
  }

  public void waitForDone() {
    if (server == null || server.version().isGreaterThanOrEquals("5.0")) {
      doWaitForDone();
    }
  }

  private void doWaitForDone() {
    boolean empty = false;
    int count = 0;
    while (!empty && count < 15) {
      Uninterruptibles.sleepUninterruptibly(delayMs, TimeUnit.MILLISECONDS);
      if (count % logFrequency == 0) {
        LOGGER.info("Waiting for analysis reports to be integrated");
      }
      try {
        String response = post(RELATIVE_URL, Collections.<String, Object>emptyMap());
        empty = "true".equals(response);
      } catch (IllegalStateException ise) {
        if (!"Unable to use reflection on SonarClient".equals(ise.getMessage())) {
          throw ise;
        }
      }
      count++;
    }
  }

  public String post(String relativeUrl, Map<String, Object> params) {
    try {
      Field field = adminWsClient().getClass().getDeclaredField("requestFactory");
      field.setAccessible(true);
      Object requestFactory = field.get(adminWsClient());
      Method post = requestFactory.getClass().getDeclaredMethod("post", String.class, Map.class);
      return (String) post.invoke(requestFactory, relativeUrl, params);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException("Unable to use reflection on SonarClient", e);
    } catch (NoSuchMethodException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
  }

  public SonarClient adminWsClient() {
    if (adminSonarClient == null && instance != null) {
      adminSonarClient = wsClient(ADMIN_LOGIN, ADMIN_PASSWORD);
    }
    return adminSonarClient;
  }

  public SonarClient wsClient(String login, String password) {
    return SonarClient.builder().url(instance).login(login).password(password).build();
  }
}
