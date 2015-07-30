/*
 * Copyright (C) 2015 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;


import com.google.common.util.concurrent.Uninterruptibles;
import com.sonar.orchestrator.container.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class SynchronousAnalyzer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SynchronousAnalyzer.class);
  static final String RELATIVE_URL = "/api/analysis_reports/is_queue_empty";

  private final Server server;
  private final long delayMs;
  private final int logFrequency;

  public SynchronousAnalyzer(Server server) {
    // check every 100ms and log every seconds
    this(server, 100L, 10);
  }

  public SynchronousAnalyzer(Server server, long delayMs, int logFrequency) {
    this.server = server;
    this.delayMs = delayMs;
    this.logFrequency = logFrequency;
  }

  public void waitForDone() {
    if (server.version().isGreaterThanOrEquals("5.0")) {
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
      String response = server.post(RELATIVE_URL, Collections.<String, Object>emptyMap());
      empty = "true".equals(response);
      Uninterruptibles.sleepUninterruptibly(delayMs, TimeUnit.MILLISECONDS);
      count++;
    }
  }
}
