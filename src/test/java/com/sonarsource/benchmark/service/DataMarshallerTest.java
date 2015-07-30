package com.sonarsource.benchmark.service;

import com.sonarsource.benchmark.domain.Cwe;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;


public class DataMarshallerTest {

  private static DataMarshaller dm = new DataMarshaller();

  private JSONParser parser = new JSONParser();


  @BeforeClass
  public static void setUp() throws Exception {

    Path path = Paths.get(DataMarshallerTest.class.getResource("/service/").getPath());
    dm.readBenchmarkTests(path);
  }

  @Test
  public void testBenchmarkTestReading() {
    assertThat(dm.getCweMap()).hasSize(11);
    assertThat(dm.getCweMap().get("CWE-22").getBenchmarkSamples().size()).isEqualTo(2630);
  }

  @Test
  public void testHandleIssues() {

    String json = "[{\"key\":\"c6fd194e-4fc4-4638-a1ea-0f398dd58f95\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest07628.java\",\"componentId\":7668,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"cmd\\\" is properly sanitized before use in this OS command.\",\"line\":70,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c704ee0b-0820-4589-a16f-369d98685ec2\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest19178.java\",\"componentId\":19218,\"project\":\"project\",\"rule\":\"squid:S2078\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure that \\\"bar\\\" is sanitized before use in this LDAP request.\",\"line\":53,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"security\"]},{\"key\":\"c71b7f58-e506-4a06-b01b-1dff84db4bd1\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest06941.java\",\"componentId\":6981,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"cmd\\\" is properly sanitized before use in this OS command.\",\"line\":74,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c71e2a81-fe91-4776-abdf-b1c97906366a\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest05719.java\",\"componentId\":5759,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"args\\\" is properly sanitized before use in this OS command.\",\"line\":69,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]},{\"key\":\"c72d3b10-74f1-4428-b854-29b1b498fdec\",\"component\":\"project:src/main/java/org/owasp/benchmark/testcode/BenchmarkTest12678.java\",\"componentId\":12718,\"project\":\"project\",\"rule\":\"squid:S2076\",\"status\":\"OPEN\",\"severity\":\"CRITICAL\",\"message\":\"Make sure \\\"cmd\\\" is properly sanitized before use in this OS command.\",\"line\":56,\"debt\":\"30min\",\"creationDate\":\"2015-07-30T15:08:03-0400\",\"updateDate\":\"2015-07-30T15:08:03-0400\",\"fUpdateAge\":\"14 minutes\",\"tags\":[\"cwe\",\"owasp-a1\",\"sans-top25-insecure\",\"security\"]}]";
    try {
      List<JSONObject> issues = (List<JSONObject>) parser.parse(json);
      dm.handleIssues(issues);
      Map<String, Cwe> map = dm.getCweMap();
      Cwe cwe78 = map.get("CWE-78");

      assertThat(cwe78.getFalsePositives()).hasSize(2);
      assertThat(cwe78.getFalseNegatives()).hasSize(1800);
      assertThat(cwe78.getTruePositives()).hasSize(2);
      assertThat(cwe78.getTrueNegatives()).hasSize(904);

    } catch (ParseException e) {
      fail("Unexpected exception thwon");
    }

  }

}
