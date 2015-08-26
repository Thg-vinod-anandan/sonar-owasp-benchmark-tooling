/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;


import com.google.common.util.concurrent.Uninterruptibles;
import com.sonar.orchestrator.container.Server;
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
    // check every 100ms and log every seconds
    this(server, 100L, 10);
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

  long getDelayMs() {
    return delayMs;
  }

  int getLogFrequency() {
    return logFrequency;
  }

  private void doWaitForDone() {
    boolean empty = false;
    int count = 0;
    while (!empty) {
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
      Uninterruptibles.sleepUninterruptibly(delayMs, TimeUnit.MILLISECONDS);
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
    } catch (Exception e) {
      throw new IllegalStateException("Unable to use reflection on SonarClient", e);
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
