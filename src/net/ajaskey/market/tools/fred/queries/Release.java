/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Original author : Andy Askey (ajaskey34@gmail.com)
 */
package net.ajaskey.market.tools.fred.queries;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

public class Release {

  private static List<Release>                releaseList  = new ArrayList<>();
  private static Set<String>                  uniqReleases = new HashSet<>();
  private final static DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder     = null;

  /**
   * Public query to return all the releases available at FRED.
   *
   * @return List of Releases
   */
  public static List<Release> queryReleases() {

    Release.releaseList.clear();

    int offset = 0;
    boolean readmore = true;
    while (readmore) {
      final int num = Release.fredReleaseQuery(offset);
      if (num < 1000) {
        readmore = false;
      }
      else {
        offset += num;
      }
    }

    return Release.releaseList;
  }

  /**
   * Private worker procedure to make specific query to FRED for all releases.
   *
   * @param offset query FRED to offset into data
   * @return
   */
  private static int fredReleaseQuery(int offset) {

    int totalProcessed = 0;

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/releases?api_key=%s&offset=%d", ApiKey.get(), offset);

      final String resp = Utils.getFromUrl(url, 6, 7);

      if (resp.length() > 0) {

        if (Release.dBuilder == null) {
          Release.dBuilder = Release.dbFactory.newDocumentBuilder();
        }

        final Document doc = Release.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("release");

        totalProcessed = nResp.getLength();

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            final Release rel = new Release();
            rel.id = eElement.getAttribute("id");
            rel.name = eElement.getAttribute("name");
            rel.realtime_start = eElement.getAttribute("realtime_start");
            rel.realtime_end = eElement.getAttribute("realtime_end");
            rel.press_release = eElement.getAttribute("press_release");
            rel.link = eElement.getAttribute("link");

            rel.setUrl(url);
            rel.setResponse(resp);

            final boolean newRel = Release.uniqReleases.add(rel.id);
            if (newRel) {
              rel.valid = true;
              Release.releaseList.add(rel);
            }
            else {
              totalProcessed--;
            }
          }
        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    return totalProcessed;
  }

  private String  id;
  private String  name;
  private String  realtime_start;
  private String  realtime_end;
  private String  press_release;
  private String  link;
  private String  url;
  private String  response;
  private boolean valid;

  /**
   * Constructor
   */
  public Release() {
    this.valid = false;
  }

  /**
   * Constructor
   *
   * @param relId
   */
  public Release(String relId) {
    this.id = relId;
    this.valid = false;
  }

  public String getId() {
    return this.id;
  }

  public String getLink() {
    return this.link;
  }

  public String getName() {
    return this.name;
  }

  public String getPress_release() {
    return this.press_release;
  }

  public String getRealtime_end() {
    return this.realtime_end;
  }

  public String getRealtime_start() {
    return this.realtime_start;
  }

  public String getResponse() {
    return this.response;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    final String ret = String.format("Release Id=%s   Name=%s    Valid=%s", this.id, this.name, this.valid);
    return ret;
  }

  void setResponse(String response) {
    this.response = response;
  }

  void setUrl(String url) {
    this.url = url;
  }

}
