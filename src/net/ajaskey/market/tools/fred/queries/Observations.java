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

import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DateValue;

/**
 * Class is used to query observation data from FRED.
 */

public class Observations {

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  public static Observations queryObservation(String id, int retries, int delay) {

    final Observations obs = new Observations(id);

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/series/observations?series_id=%s&api_key=%s", id, ApiKey.get());

      final String resp = Utils.getFromUrl(url, retries, delay);

      if (resp.length() > 0) {

        if (Observations.dBuilder == null) {
          Observations.dBuilder = Observations.dbFactory.newDocumentBuilder();
        }

        final Document doc = Observations.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        NodeList nResp = doc.getElementsByTagName("observations");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            obs.realtimeStart = eElement.getAttribute("realtime_start");
            obs.realtimeEnd = eElement.getAttribute("realtime_end");
            obs.observationStart = eElement.getAttribute("observation_start");
            obs.observationEnd = eElement.getAttribute("observation_end");
            obs.units = eElement.getAttribute("units");
            obs.outputType = eElement.getAttribute("output_type");
            obs.fileType = eElement.getAttribute("file_type");
            obs.orderBy = eElement.getAttribute("order_by");
            obs.sortOrder = eElement.getAttribute("sort_order");
            String tmp = eElement.getAttribute("count");
            obs.count = Integer.parseInt(tmp);
            tmp = eElement.getAttribute("offset");
            obs.offset = Integer.parseInt(tmp);
            tmp = eElement.getAttribute("limit");
            obs.limit = Integer.parseInt(tmp);
          }
        }

        nResp = doc.getElementsByTagName("observation");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            final String sDate = eElement.getAttribute("date");
            final String sValue = eElement.getAttribute("value");

            // System.out.printf("%s\t%s%n", sDate, sValue);

            final DateValue dv = new DateValue(sDate, sValue);
            if (dv.isValid()) {
              obs.dvList.add(dv);
            }
          }
        }
        obs.valid = true;
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
      obs.valid = false;
    }

    return obs;
  }

  private final String id;

  private String                realtimeStart;
  private String                realtimeEnd;
  private String                observationStart;
  private String                observationEnd;
  private String                units;
  private String                outputType;
  private String                fileType;
  private String                orderBy;
  private String                sortOrder;
  private int                   count;
  private int                   offset;
  private int                   limit;
  private boolean               valid;
  private final List<DateValue> dvList;

  /**
   * Constructor
   *
   * @param id
   */
  public Observations(String id) {
    this.id = id;
    this.valid = false;
    this.dvList = new ArrayList<>();
  }

  public int getCount() {
    return this.count;
  }

  public List<DateValue> getDvList() {
    return this.dvList;
  }

  public String getFileType() {
    return this.fileType;
  }

  public String getId() {
    return this.id;
  }

  public int getLimit() {
    return this.limit;
  }

  public String getObservationEnd() {
    return this.observationEnd;
  }

  public String getObservationStart() {
    return this.observationStart;
  }

  public int getOffset() {
    return this.offset;
  }

  public String getOrderBy() {
    return this.orderBy;
  }

  public String getOutputType() {
    return this.outputType;
  }

  public String getRealtimeEnd() {
    return this.realtimeEnd;
  }

  public String getRealtimeStart() {
    return this.realtimeStart;
  }

  public String getSortOrder() {
    return this.sortOrder;
  }

  public String getUnits() {
    return this.units;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    String ret = "Id                : " + this.id + Utils.NL;
    ret += "Units             : " + this.units + Utils.NL;
    ret += "Output Type       : " + this.outputType + Utils.NL;
    ret += "File Type         : " + this.fileType + Utils.NL;
    ret += "Sort Order        : " + this.sortOrder + Utils.NL;
    ret += "Order By          : " + this.orderBy + Utils.NL;
    ret += "Offset            : " + this.offset + Utils.NL;
    ret += "Limit             : " + this.limit + Utils.NL;
//    ret += "Realtime Start    : " + this.realtimeStart + Utils.NL;
//    ret += "Realtime End      : " + this.realtimeEnd + Utils.NL;
//    ret += "Observation Start : " + this.observationStart + Utils.NL;
//    ret += "Observation End   : " + this.observationEnd + Utils.NL;
    ret += "Count             : " + this.count + Utils.NL;
    ret += String.format("First/Last Date   : %s  %s", this.dvList.get(0).getDate(), this.dvList.get(this.dvList.size() - 1).getDate());
//    for (DateValue dv : this.dvList) {
//      ret += " Date / Value     : " + dv.getDate() + " / " + dv.getValue() + Utils.NL;
//    }

    return ret;
  }

}
