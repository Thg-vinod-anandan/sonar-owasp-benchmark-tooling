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
package org.sonarsource.benchmark.get;

import org.sonarsource.benchmark.domain.ReportException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Fetcher {

  public List<JSONObject> fetchRulesFromSonarQube(String instance, String search) {

    String baseUrl = instance + "/api/rules/search?ps=500&" + search;
    return fetchPaginatedDataFromSonarQube(baseUrl, "rules");
  }

  public List<JSONObject> fetchIssuesFromSonarQube(String instance) {

    String path = "/api/issues/search?ps=500";
    return fetchPaginatedDataFromSonarQube(instance + path, "issues");
  }

  public List<JSONObject> fetchProfilesFromSonarQube(String instance) {
    JSONObject rawResult = getJsonFromUrl(instance + "/api/rules/app");
    return (List<JSONObject>) rawResult.get("qualityprofiles");
  }

  public List<JSONObject> fetchPaginatedDataFromSonarQube(String url, String dataId) {

    int page = 1;
    long expected = 100;

    ArrayList<JSONObject> results = new ArrayList<>();
    while (expected > results.size()) {

      JSONObject rawResult = getJsonFromUrl(url + "&p=" + page);
      results.addAll((JSONArray) rawResult.get(dataId));
      expected = (long) rawResult.get("total");
      page++;
    }
    return results;

  }

  public JSONObject getJsonFromPost(String url, String login, String password, Map<String,String> params){

    Client client = ClientBuilder.newClient();
    if (login != null && password != null) {
      client.register(HttpAuthenticationFeature.basic(login, password));
    }

    WebTarget webResource = client.target(url);

    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    for (Map.Entry<String,String> entry : params.entrySet()) {
      formData.add(entry.getKey(), entry.getValue());
    }

    Response response = webResource.request().accept("application/json").post(Entity.form(formData));

    checkStatus(url, client, response);

    String responseStr = response.readEntity(String.class);

    response.close();
    client.close();

    JSONParser parser = new JSONParser();
    try {
      return (JSONObject)parser.parse(responseStr);
    } catch (ParseException e) {
      throw new ReportException(e);
    }

  }

  private static JSONObject getJsonFromUrl(String url) {

    Client client = ClientBuilder.newClient();

    WebTarget webResource = client.target(url);

    Response response = webResource.request().accept("application/json").get(Response.class);

    checkStatus(url, client, response);

    String responseStr = response.readEntity(String.class);

    response.close();
    client.close();

    JSONParser parser = new JSONParser();
    try {
      return (JSONObject)parser.parse(responseStr);
    } catch (ParseException e) {
      throw new ReportException(e);
    }
  }

  private static void checkStatus(String url, Client client, Response response) {

    int status = response.getStatus();
    if (status < 200 || status > 299) {
      response.close();
      client.close();
      throw new ReportException("Failed : HTTP error code: "
              + response.getStatus() + " for " + url);
    }
  }

  public Path getFilesFromUrl(String url) {

    Path root = null;

    Client client = ClientBuilder.newClient();
    WebTarget webResource = client.target(url);
    Response response = webResource.request().accept("application/zip").get(Response.class);
    checkStatus(url, client, response);

    FileOutputStream output = null;
    try(InputStream is = response.readEntity(InputStream.class); ZipInputStream zin = new ZipInputStream(is)) {

      Files.createDirectories(Paths.get("target"));

      byte[] buffer = new byte[2048];
      ZipEntry entry;
      while((entry = zin.getNextEntry())!=null) {

        String outpath = "target/" + entry.getName();

        File file = new File(outpath);

        if (entry.isDirectory()) {
          if (root == null) {
            root = file.toPath();
          }
          file.mkdir();
          continue;
        }

        output = new FileOutputStream(file);

        int len = 0;
        while ((len = zin.read(buffer)) > 0) {
          output.write(buffer, 0, len);
        }
        output.close();

      }
    } catch (IOException e) {
      throw new ReportException(e);

    } finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          // intentionally blank
        }
      }

      response.close();
      client.close();
    }
    return root;
  }

}
