package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.BenchmarkTest;
import com.sonarsource.benchmark.domain.Constants;
import com.sonarsource.benchmark.domain.Cwe;
import com.sonarsource.benchmark.domain.RuleException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Reporter {

  private static final Logger LOGGER = Logger.getLogger(Reporter.class.getName());
  private static final String REPORT_PATH = "target/reports/";
  private static final String TD = "</td><td>";

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

      generateBadResultsReport(cwe);

      sb.append("<tr><td>");
      addLinkedCweId(cwe, sb);
      sb.append(TD).append(cwe.getRuleKeys().size())
              .append(TD).append(cwe.getIssueCount())
              .append(TD).append(cwe.getTruePositives().size())
              .append(TD).append(cwe.getTrueNegatives().size())
              .append(TD).append(cwe.getFalsePositives().size())
              .append(TD).append(cwe.getFalseNegatives().size())
              .append(TD).append("<a href='").append(cwe.getId()).append(".html#fp'>").append(String.format("%.2f%%", cwe.getPositiveAccuracy())).append("</a>")
              .append(TD).append("<a href='").append(cwe.getId()).append(".html#fn'>").append(String.format("%.2f%%", cwe.getNegativeAccuracy())).append("</a>")
              .append("</tr>");
    }
    sb.append("</table>");

    writeFile(REPORT_PATH + "summary.html", sb.toString());
  }

  public void generateBadResultsReport(Cwe cwe) {

    StringBuilder sb = getHtmlStringBuilder();
    sb.append("<h2>");
    addLinkedCweId(cwe, sb);
    sb.append("</h2>");

    sb.append("<a href='#fp'>False Positives (").append(cwe.getFalsePositives().size()).append(")</a> | ");
    sb.append("<a href='#fn'>False Negatives (").append(cwe.getFalseNegatives().size()).append(")</a>");

    sb.append("<a id='fp' name='fp'></a><h3>False Positives (").append(cwe.getFalsePositives().size()).append(")</h3>");

    sb.append("<table>");
    for (BenchmarkTest bt : cwe.getFalsePositives()) {
      sb.append("<tr><td>");
      for (String rule : bt.getIssueRules()) {
        sb.append(rule).append(" ");
      }
      sb.append(TD).append("<a href='")
              .append(Constants.BENCHMARK_GIT_PROJECT)
              .append(Constants.BENCHMARK_TEST_PATH)
              .append(bt.getFileName()).append(".java'>");
      sb.append(bt.getFileName()).append("</a></td></tr>");
    }
    sb.append("</table>");

    sb.append("<a id='fn' name='fn'></a><h3>False Negatives (").append(cwe.getFalseNegatives().size()).append(")</h3>");
    sb.append("<table>");
    for (BenchmarkTest bt : cwe.getFalseNegatives()) {
      sb.append("<tr><td>");
      sb.append("<a href='")
              .append(Constants.BENCHMARK_GIT_PROJECT)
              .append(Constants.BENCHMARK_TEST_PATH)
              .append(bt.getFileName()).append(".java'>");
      sb.append(bt.getFileName()).append("</a></td></tr>");
    }
    sb.append("</table>");

    writeFile(REPORT_PATH + cwe.getId() + ".html", sb.toString());
  }

  protected void addLinkedCweId(Cwe cwe, StringBuilder sb) {

    sb.append("<a href='").append(Constants.CWE_URL_ROOT).append(cwe.getNumber()).append("'>")
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
      throw new RuleException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuleException(e);
    }
  }

}
