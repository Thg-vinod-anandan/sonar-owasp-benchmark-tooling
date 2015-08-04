/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.BenchmarkSample;
import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.domain.Cwe;
import com.sonarsource.benchmark.domain.ReportException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Reporter {

  private static final Logger LOGGER = Logger.getLogger(Reporter.class.getName());
  private static final String REPORT_PATH = "target/reports/owasp-benchmark/";
  private static final String TD = "</td><td>";
  private static final String TR_TD = "<tr><td>";
  private static final String TABLE_OPEN = "<table>";
  private static final String TABLE_CLOSE = "</table>";
  public static final String A_HREF = "<a href='";

  private String css = "";


  public Reporter() {
    java.net.URL url = this.getClass().getResource("/service");
    if (url != null) {
      try {
        css = new java.util.Scanner(new File(url.getPath() + "/report.css"), "UTF8").useDelimiter("\\Z").next();
      } catch (FileNotFoundException e) {
        LOGGER.log(Level.WARNING, "CSS file not found", e);
      }
    }
  }

  public void generateReports(DataMarshaller gatherer) {

    StringBuilder sb = getHtmlStringBuilder();
    sb.append("<table><tr><th rowspan='2'>CWE</th>")
            .append("<th rowspan='2'># rules</th><th rowspan='2'># issues</th>")
            .append("<th colspan='2'>True</th><th colspan='2'>False</th>")
            .append("<th>Accuracy</th></tr>")
            .append("<tr><th>Pos</th><th>Neg</th><th>Pos</th><th>Neg</th><th>Pos</th><th>Neg</th></tr>");

    for (Cwe cwe : gatherer.getCweMap().values()) {

      writeBadResultsReport(cwe);

      sb.append(TR_TD);
      addLinkedCweId(cwe, sb);
      sb.append(TD).append(cwe.getRuleKeys().size())
              .append(TD).append(cwe.getIssueCount())
              .append(TD).append(cwe.getTruePositives().size())
              .append(TD).append(cwe.getTrueNegatives().size())
              .append(TD).append(cwe.getFalsePositives().size())
              .append(TD).append(cwe.getFalseNegatives().size())
              .append(TD).append(A_HREF).append(cwe.getId()).append(".html#fp'>").append(String.format("%.2f%%", cwe.getPositiveAccuracyPercentage())).append("</a>")
              .append(TD).append(A_HREF).append(cwe.getId()).append(".html#fn'>").append(String.format("%.2f%%", cwe.getNegativeAccuracyPercentage())).append("</a>")
              .append("</tr>");
    }
    sb.append(TABLE_CLOSE);

    writeFile(REPORT_PATH + "summary.html", sb.toString());
  }

  public void writeBadResultsReport(Cwe cwe) {

    writeFile(REPORT_PATH + cwe.getId() + ".html", generateBadResultsReport(cwe));
  }

  protected String generateBadResultsReport(Cwe cwe) {

    StringBuilder sb = getHtmlStringBuilder();
    sb.append("<h2>");
    addLinkedCweId(cwe, sb);
    sb.append("</h2>");

    sb.append("<a href='#fp'>False Positives (").append(cwe.getFalsePositives().size()).append(")</a> | ");
    sb.append("<a href='#fn'>False Negatives (").append(cwe.getFalseNegatives().size()).append(")</a>");

    sb.append("<a id='fp' name='fp'></a><h3>False Positives (").append(cwe.getFalsePositives().size()).append(")</h3>");

    sb.append(TABLE_OPEN);
    for (BenchmarkSample bt : cwe.getFalsePositives()) {
      sb.append(TR_TD);
      for (String rule : bt.getIssueRules()) {
        sb.append(rule).append(" ");
      }
      sb.append(TD).append(A_HREF)
              .append(Constants.BENCHMARK_GIT_PROJECT)
              .append(Constants.BENCHMARK_TEST_PATH)
              .append(bt.getFileName()).append(".java'>");
      sb.append(bt.getFileName()).append("</a></td></tr>");
    }
    sb.append(TABLE_CLOSE);

    sb.append("<a id='fn' name='fn'></a><h3>False Negatives (").append(cwe.getFalseNegatives().size()).append(")</h3>");
    sb.append(TABLE_OPEN);
    for (BenchmarkSample bt : cwe.getFalseNegatives()) {
      sb.append(TR_TD);
      sb.append(A_HREF)
              .append(Constants.BENCHMARK_GIT_PROJECT)
              .append(Constants.BENCHMARK_TEST_PATH)
              .append(bt.getFileName()).append(".java'>");
      sb.append(bt.getFileName()).append("</a></td></tr>");
    }
    sb.append(TABLE_CLOSE);

    return sb.toString();
  }

  protected void addLinkedCweId(Cwe cwe, StringBuilder sb) {

    sb.append(A_HREF).append(Constants.CWE_URL_ROOT).append(cwe.getNumber()).append("'>")
            .append(cwe.getId())
            .append("</a>");
  }

  protected StringBuilder getHtmlStringBuilder(){
    StringBuilder sb = new StringBuilder();
    sb.append("<html><head>").append(css).append("</head><body>");
    return sb;
  }

  private static void writeFile(String fileName, String content) {
    if (content == null) {
      return;
    }

    PrintWriter writer = null;
    try {
      LOGGER.info("Writing " + fileName);
      String path = fileName.replaceAll(" ", "_");

      File file = new File(path);
      File parent = file.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }

      writer = new PrintWriter(file, "UTF-8");
      writer.println(content);
      writer.close();

    } catch (FileNotFoundException e) {
      throw new ReportException(e);
    } catch (UnsupportedEncodingException e) {
      throw new ReportException(e);
    }
  }

}
