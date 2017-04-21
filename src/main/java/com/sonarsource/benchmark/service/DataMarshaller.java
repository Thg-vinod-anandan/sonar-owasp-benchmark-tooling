/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.BenchmarkSample;
import com.sonarsource.benchmark.domain.Cwe;
import com.sonarsource.benchmark.domain.ReportException;
import com.sonarsource.benchmark.get.Fetcher;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DataMarshaller {

  private static final Logger LOGGER = Logger.getLogger(DataMarshaller.class.getName());

  private Map<String, Cwe> cweMap = new HashMap<>();
  private Map<String, BenchmarkSample> btMap = new HashMap<>();
  private Fetcher fetcher = new Fetcher();
  private List<JSONObject> rawIssues = null;


  public void addIssuesToBenchmarkTests(String instance) {

    LOGGER.info("Processing issues");

    handleIssues(fetcher.fetchIssuesFromSonarQube(instance));
  }

  protected void handleIssues(List<JSONObject> jsonIssues) {

    rawIssues = jsonIssues;

    for (Object obj : jsonIssues) {
      JSONObject jObj = (JSONObject) obj;
      String ruleKey = (String) jObj.get("rule");
      String filePath = (String) jObj.get("component");

      String[] pieces = filePath.split("/");
      String testName = pieces[pieces.length - 1].replaceAll(".java", "");
      BenchmarkSample bt = btMap.get(testName);
      if (bt != null) {
        bt.addIssueRule(ruleKey);
      } else {
        LOGGER.log(Level.INFO, "Unrecognized fileName: {0}", testName);
      }
    }

    for (Cwe cwe : cweMap.values()) {
      cwe.sortResults();
    }
  }

  public void readBenchmarkTests(Path path) {

    LOGGER.info("Reading expected results");

    File file = getExpectedResultsFile(path);

    try {
      if (file != null) {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
        for (int i = 1; i < lines.size(); i++) {
          mapFileToCwe(lines.get(i));

        }
      }
    } catch (IOException e) {
      throw new ReportException(e);
    }
  }

  protected void mapFileToCwe(String line) {

    String[] pieces = line.split(",");

    String fileName = pieces[0];
    int cweNumber = Integer.parseInt(pieces[3]);
    String cweId = "CWE-" + cweNumber;

    BenchmarkSample bt = new BenchmarkSample(fileName, Boolean.valueOf(pieces[2]));
    btMap.put(fileName, bt);

    Cwe cwe = cweMap.get(cweId);
    if (cwe == null) {
      cwe = new Cwe(cweNumber);
      cweMap.put(cweId, cwe);
    }
    cwe.addBenchmarkSample(bt);
  }

  protected File getExpectedResultsFile(Path path) {

    File file = null;

    File dir = path.toFile();
    if (dir.isDirectory()) {
      List<String> resultsFileNames = new ArrayList<>();
      File[] files = dir.listFiles();
      for (File f : files) {
        if (f.isFile() && f.getName().matches("expectedresults.*\\.csv")) {
          resultsFileNames.add(f.getName());
        }
      }
      if (resultsFileNames.size() == 1) {
        file = path.resolve(resultsFileNames.get(0)).toFile();
      } else if (!resultsFileNames.isEmpty()){
        Collections.sort(resultsFileNames);
        file = path.resolve(resultsFileNames.get(resultsFileNames.size()-1)).toFile();
      }

    }
    return file;
  }

  public void activateCweRules(String instance) {

    String admin = "admin";

    LOGGER.info("activating CWE rules");

    List<JSONObject> qualityProfiles = fetcher.fetchProfilesFromSonarQube(instance);
    String profileKey = (String) qualityProfiles.get(0).get("key");

    Map params = new HashMap();
    params.put("activation", "true");
    params.put("profile_key", profileKey);

    fetcher.getJsonFromPost(instance + "/api/qualityprofiles/deactivate_rules", admin, admin, params);

    for (Cwe cwe : cweMap.values()) {
      // not possible to set field list (f=) to just key, so set it to "internalKey" so we don't get all fields
      List<JSONObject> rules = fetcher.fetchRulesFromSonarQube(instance, "f=internalKey&q=" + cwe.getId());

      for (JSONObject jobj : rules) {
        cwe.addRuleKey((String) jobj.get("key"));
      }

      if (!rules.isEmpty()) {
        params = new HashMap();
        params.put("profile_key", profileKey);
        params.put("q", cwe.getId());

        fetcher.getJsonFromPost(instance + "/api/qualityprofiles/activate_rules", admin, admin, params);
      }
    }
  }

  public Map<String, Cwe> getCweMap() {

    return cweMap;
  }

  public List<JSONObject> getRawIssues() {

    return rawIssues;
  }
}
