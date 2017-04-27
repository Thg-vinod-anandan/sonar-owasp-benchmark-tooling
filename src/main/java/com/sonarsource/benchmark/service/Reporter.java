/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.BenchmarkSample;
import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.domain.Cwe;
import com.sonarsource.benchmark.domain.ReportException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;
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
    try (Scanner scanner = new java.util.Scanner(new File(url.getPath() + "/report.css"), "UTF8")) {
      css = scanner.useDelimiter("\\Z").next();
    } catch (FileNotFoundException e) {
      LOGGER.log(Level.WARNING, "CSS file not found", e);
    }
  }

  public void generateReports(DataMarshaller gatherer) {

    writeJsonReport(gatherer.getRawIssues());

    StringBuilder sb = getHtmlStringBuilder();
    sb.append("<h2>OWASP Benchmark results - Java</h2>")
            .append("<table><tr><th rowspan='2'>CWE</th>")
            .append("<th rowspan='2'># rules</th><th rowspan='2'># issues</th>")
            .append("<th colspan='2'>True</th><th colspan='2'>False</th>")
            .append("<th colspan='2'>Accuracy</th>")
            .append("<th rowspan='2'>Score</th>")
            .append("<th rowspan='2'>Unexpected</th>")
            .append("</tr>")
            .append("<tr><th>Pos</th><th>Neg</th><th>Pos</th><th>Neg</th><th>Pos</th><th>Neg</th></tr>");

    String twoDecimalPercent = "%.2f%%";

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
              .append(TD).append(A_HREF).append(cwe.getId()).append(".html#fp'>").append(String.format(twoDecimalPercent, cwe.getPositiveAccuracyPercentage())).append("</a>")
              .append(TD).append(A_HREF).append(cwe.getId()).append(".html#fn'>").append(String.format(twoDecimalPercent, cwe.getNegativeAccuracyPercentage())).append("</a>")
              .append(TD).append(String.format(twoDecimalPercent,cwe.getPositiveAccuracyPercentage()-cwe.getNegativeAccuracyPercentage()))
              .append(TD).append(cwe.getUnexpectedIssues().size())
              .append("</td></tr>");

    }
    sb.append(TABLE_CLOSE);

    writeFile(REPORT_PATH + "summary.html", sb.toString());
  }

  public void writeJsonReport(List<JSONObject> issues) {

    JSONArray jsonArray = new JSONArray();
    jsonArray.addAll(issues);

    writeFile(REPORT_PATH + "raw.json", jsonArray.toJSONString());

  }

  public void writeBadResultsReport(Cwe cwe) {

    writeFile(REPORT_PATH + cwe.getId() + ".html", generateBadResultsReport(cwe));
  }

  protected String generateBadResultsReport(Cwe cwe) {

    StringBuilder sb = getHtmlStringBuilder();
    sb.append("<h2>");
    addLinkedCweId(cwe, sb);
    sb.append("</h2>");

    sb.append("<a href='summary.html'>Back to Summary</a><br/>");
    sb.append("<a href='#fp'>False Positives (").append(cwe.getFalsePositives().size()).append(")</a> | ");
    sb.append("<a href='#fn'>False Negatives (").append(cwe.getFalseNegatives().size()).append(")</a>");

    sb.append("<h3>Rules</h3>");
    for (String key : cwe.getRuleKeys()) {
      String k = key.split(":")[1];
      sb.append("<a href='http://jira.sonarsource.com/browse/RSPEC-")
              .append(k.replaceAll("S", ""))
              .append("'>").append(k).append("</a><br/>");
    }

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

    LOGGER.log(Level.INFO, "Writing {0}", fileName);
    String path = fileName.replaceAll(" ", "_");

    File file = new File(path);
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }

    try (PrintWriter writer = new PrintWriter(file, "UTF-8")){
      writer.println(content);
    } catch (FileNotFoundException|UnsupportedEncodingException  e) {
      throw new ReportException(e);
    }
  }

}
