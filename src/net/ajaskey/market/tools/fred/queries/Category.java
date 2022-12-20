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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

public class Category {

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  /**
   *
   * @param args
   */
  public static void main(String[] args) {

    ApiKey.set();
    Debug.init("debug/Category.dbg", java.util.logging.Level.INFO);

  }

  public static List<Category> queryCategoriesPerSeries(String series_id, int retries, int delay) {

    final List<Category> catList = new ArrayList<>();

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/series/categories?series_id=%s&api_key=&api_key=%s", series_id, ApiKey.get());

      final String resp = Utils.getFromUrl(url, retries, delay);

      if (resp.length() > 0) {
        if (Category.dBuilder == null) {
          Category.dBuilder = Category.dbFactory.newDocumentBuilder();
        }

        final Document doc = Category.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("category");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Category cat = new Category();

            final Element eElement = (Element) nodeResp;

            cat.id = eElement.getAttribute("id").trim();
            cat.name = eElement.getAttribute("name").trim();
            cat.parent_id = eElement.getAttribute("parent_id").trim();
            cat.valid = true;
            catList.add(cat);

            Debug.LOGGER.info(String.format("Processed Serid_id=%s  cat : %s", series_id, cat));
          }
        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    return catList;
  }

  /**
   *
   * @param id Numerical id of the FRED category.
   * @return Data from the category
   */
  public static Category queryCategory(int id, int retries, int delay) {

    final Category cat = new Category();

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/category?category_id=%d&api_key=&api_key=%s", id, ApiKey.get());

      final String resp = Utils.getFromUrl(url, retries, delay);

      if (resp.length() > 0) {

        if (Category.dBuilder == null) {
          Category.dBuilder = Category.dbFactory.newDocumentBuilder();
        }

        final Document doc = Category.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("category");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            cat.id = eElement.getAttribute("id").trim();
            cat.name = eElement.getAttribute("name").trim();
            cat.parent_id = eElement.getAttribute("parent_id").trim();
            cat.valid = true;

          }

        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }
    return cat;

  }

  private String id;

  private String name;

  private String parent_id;

  private boolean valid;

  /**
   *
   */
  public Category() {
    this.valid = false;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    final String ret = String.format("id=%5s    name=%s   parent_id=%s", this.id, this.name, this.parent_id);
    return ret;
  }

  String getId() {
    return this.id;
  }

  String getName() {
    return this.name;
  }

  String getParentId() {
    return this.parent_id;
  }

}
