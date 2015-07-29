/*
 * Copyright (C) 2015 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.issues;

import com.sonarsource.benchmark.domain.BenchmarkTest;
import com.sonarsource.benchmark.domain.Cwe;
import com.sonarsource.benchmark.domain.RuleException;
import com.sonarsource.benchmark.get.Fetcher;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataGatherer {

  private Map<String, Cwe> cweMap = new HashMap<>();
  private Map<String, BenchmarkTest> btMap = new HashMap<>();
  private Fetcher fetcher = new Fetcher();


  public void addIssuesToBenchmarkTests(String instance) {
    Map<String,List<String>> ruleIssues = new HashMap<>();

    List<JSONObject> jsonIssues = fetcher.fetchIssuesFromSonarQube(instance);

    for (Object obj : jsonIssues) {
      JSONObject jObj = (JSONObject) obj;
      String ruleKey = (String) jObj.get("rule");
      String filePath = (String) jObj.get("component");

      String[] pieces = filePath.split("/");
      String testName = pieces[pieces.length - 1].replaceAll(".java", "");
      BenchmarkTest bt = btMap.get(testName);
      if (bt != null) {
        bt.getIssueRules().add(ruleKey);
      } else {
/// log bad name...
      }
    }
  }

  public void readBenchmarkTests(Path path) {

    File file = path.resolve("expectedresults-1.1.csv").toFile();
    try {

      List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
      for (int i = 1; i < lines.size(); i++ ) {
        String line = lines.get(i);
        String[] pieces = line.split(",");

        String fileName = pieces[0];
        String cweId = "CWE-" + pieces[3];

        BenchmarkTest bt = new BenchmarkTest(fileName, Boolean.valueOf(pieces[2]), cweId);
        btMap.put(fileName, bt);

        Cwe cwe = cweMap.get(cweId);
        if (cwe == null) {
          cwe = new Cwe(cweId);
          cweMap.put(cweId, cwe);
        }
        cwe.addBenchmarkTest(bt);

      }

    } catch (IOException e) {
      throw new RuleException(e);
    }
  }

  public void activateCweRules(String instance) {

    List<JSONObject> qualityProfiles = fetcher.fetchProfilesFromSonarQube(instance);
    String profileKey = (String) qualityProfiles.get(0).get("key");

    Map params = new HashMap();
    params.put("activation", "true");
    params.put("profile_key", profileKey);

    fetcher.getJsonFromPost(instance + "/api/qualityprofiles/deactivate_rules", "admin", "admin", params);

    for (Cwe cwe : cweMap.values()) {
      // not possible to set field list (f=) to just key, so set it to "internalKey" so we don't get all fields
      List<JSONObject> rules = fetcher.fetchRulesFromSonarQube(instance, "f=internalKey&q=" + cwe.getId());

      for (JSONObject jobj : rules) {
        cwe.addRuleKey((String) jobj.get("key"));
      }

      if (rules.size() > 0) {
        params = new HashMap();
        params.put("profile_key", profileKey);
        params.put("q", cwe.getId());

        fetcher.getJsonFromPost(instance + "/api/qualityprofiles/activate_rules", "admin", "admin", params);
      }
    }
  }

}
