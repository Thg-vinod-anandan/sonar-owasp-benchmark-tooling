/*
 * Copyright (C) 2015-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonarsource.benchmark.get;

import com.sonarsource.benchmark.domain.ReportException;
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

    MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
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

  private JSONObject getJsonFromUrl(String url) {

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

  private void checkStatus(String url, Client client, Response response) {

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
