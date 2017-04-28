/*
 * Copyright (C) 2015-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.Cwe;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestUtilities {

  protected DataMarshaller getDataMarshallerWithBenchmarkTests(){
    DataMarshaller dm = new DataMarshaller();

    String here = DataMarshallerTest.class.getResource("/service/").getPath();
    if (System.getProperty("os.name").contains("indow")) {
      here = here.substring(1);
    }

    Path path = Paths.get(here);
    dm.readBenchmarkTests(path);

    return dm;
  }

  protected DataMarshaller getDataMarshallerWithBenchmarkTestsAndCwes() throws ParseException {
    DataMarshaller dm = getDataMarshallerWithBenchmarkTests();

    Map<String, Cwe> map = dm.getCweMap();
    Cwe cwe78 = map.get("CWE-78");
    cwe78.addRuleKey("squid:S2076");
    cwe78.addRuleKey("squid:S2978");

    JSONParser parser = new JSONParser();
    String json = "[{\"key\":\"c6fd194e-4fc4-4638-a1ea-0f398dd58f95\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest07628.java\",\"componentId\":7668,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"cmd\\\" is properly sanitized before use in this OS command.\",\"line\":70,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c704ee0b-0820-4589-a16f-369d98685ec2\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest19178.java\",\"componentId\":19218,\"project\":\"project\",\"rule\":\"squid:S2078\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure that \\\"bar\\\" is sanitized before use in this LDAP request.\",\"line\":53,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"security\"]},{\"key\":\"c71b7f58-e506-4a06-b01b-1dff84db4bd1\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest06941.java\",\"componentId\":6981,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"cmd\\\" is properly sanitized before use in this OS command.\",\"line\":74,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c71e2a81-fe91-4776-abdf-b1c97906366a\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest05719.java\",\"componentId\":5759,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"args\\\" is properly sanitized before use in this OS command.\",\"line\":69,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c71e2a81-fe91-4776-abdf-b1c97906367b\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/bogus.java\",\"componentId\":5760,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"args\\\" is properly sanitized before use in this OS command.\",\"line\":69,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c72d3b10-74f1-4428-b854-29b1b498fdec\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest12678.java\",\"componentId\":12718,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"cmd\\\" is properly sanitized before use in this OS command.\",\"line\":56,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]}]\n";
    List<JSONObject> issues = (List<JSONObject>) parser.parse(json);
    dm.handleIssues(issues);

    return dm;
  }

}
