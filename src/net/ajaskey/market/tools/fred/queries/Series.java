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

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeries.ResponseType;

public class Series {

  public final static SimpleDateFormat        sdf       = new SimpleDateFormat("dd-MMM-yyyy");
  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  private static List<Series> seriesList = new ArrayList<>();
  private static Set<String>  uniqSeries = new HashSet<>();

  /**
   *
   * @param id
   * @param retries
   * @param delay
   * @param fredlib
   * @return
   */
  public static Series query(String id, int retries, int delay) {

    Series ser = new Series();

    try {

      // URL to query a list of series associated with the release_id parameter
      final String url = String.format("https://api.stlouisfed.org/fred/series?series_id=%s&api_key=%s", id, ApiKey.get());

      // XML representing the series data
      final String resp = Utils.getFromUrl(url, retries, delay);

      ser = Series.processResponse(resp, url);

    }
    catch (final Exception e) {
      e.printStackTrace();
      ser.setValid(false);
    }

    Debug.LOGGER.info(String.format("Returning from query."));

    return ser;
  }

  /**
   * Query FRED for all Series within a Release. Note that FRED returns a maximum
   * of 1000 series per query.
   *
   * If data of 1000 Series are found then another query is run for 1000 more
   * (using the offset parameter). Logic in the processing method determines if
   * FRED has sent the same data twice. A return of less than 1000 series means
   * that all the data has been found at FRED and the queries stop.
   *
   * @param release_id
   * @param retries
   * @param delay
   * @return
   */
  public static List<Series> querySeriesPerRelease(String release_id, int retries, int delay) {

    Series.seriesList.clear();

    int offset = 0;
    boolean readmore = true;
    while (readmore) {
      Debug.LOGGER.info(String.format("%n---------%ncalling fredSeriesPerSeriesQuery.  release_id=%s  offset=%d", release_id, offset));

      final int num = Series.processSeriesPerReleaseQuery(release_id, offset, retries, delay);

      if (num < 1000) {
        readmore = false;
      }
      else {
        offset += num;
      }
    }

    return Series.seriesList;
  }

  /**
   * Private method performing actual query to FRED.
   *
   * @param response
   * @param url
   * @param fredlib
   * @return One instance of Series data
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  private static Series processResponse(String response, String url) throws SAXException, IOException, ParserConfigurationException {

    final Series ser = new Series();

    if (response.length() > 0) {

      if (Series.dBuilder == null) {
        Series.dBuilder = Series.dbFactory.newDocumentBuilder();
      }

      final Document doc = Series.dBuilder.parse(new InputSource(new StringReader(response)));

      doc.getDocumentElement().normalize();

      final NodeList nResp = doc.getElementsByTagName("series");

      for (int knt = 0; knt < nResp.getLength(); knt++) {

        final Node nodeResp = nResp.item(knt);

        if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
          final Element eElement = (Element) nodeResp;

          ser.setUrl(url);

          ser.setId(eElement.getAttribute("id").trim());
          ser.setTitle(eElement.getAttribute("title").trim());
          ser.setFrequency(eElement.getAttribute("frequency").trim());
          ser.setUnits(eElement.getAttribute("units").trim());
          ser.setType(ResponseType.LIN);
          ser.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment").trim());
          ser.setSeasonalAdjustmentShort(eElement.getAttribute("seasonal_adjustment_short").trim());
          ser.setLastUpdate(eElement.getAttribute("last_updated").trim());
          ser.setFirstObservation(eElement.getAttribute("observation_start").trim());
          ser.setLastObservation(eElement.getAttribute("observation_end").trim());
          ser.notes = eElement.getAttribute("notes").trim();

          final boolean newSer = Series.uniqSeries.add(ser.id);
          if (newSer) {

            ser.setValid(true);

          }
          else {
            /**
             * This id has already been read. Decrement totalProcess (now less than 1000).
             * Calling process will know query response contained duplicated data and there
             * is nothing new in the FRED database.
             */
            Debug.LOGGER.info(String.format("Duplicate series not added!%s", ser.getId()));
          }
        }
      }
    }
    else {
      Debug.LOGGER.info("Warning. No response data returned from FRED.");
    }

    return ser;
  }

  /**
   *
   * Private method performing actual query to FRED.
   *
   * If the query returns 1000 items (max from FRED) then it is assumed more data
   * is available.
   *
   * Another query is executed with the offset parameter incremented by 1000.
   *
   * Experiments have found the FRED often returns two blocks of 1000 data items
   * when only 1500 are available. Processing stops an Id to be added to the data
   * more than once.
   *
   * The total number of unique items is returned. When this number is less than
   * 1000 then caller should stop the query loop.
   *
   * @param release_id Id of the Release
   * @param offset
   * @param retries
   * @param delay
   * @return a List of Query data
   */
  private static int processSeriesPerReleaseQuery(String release_id, int offset, int retries, int delay) {

    int totalProcessed = 0;

    try {

      // URL to query a list of series associated with the release_id parameter
      final String url = String.format("https://api.stlouisfed.org/fred/release/series?release_id=%s&api_key=%s&offset=%d", release_id, ApiKey.get(),
          offset);

      // XML representing all series associated with the release id
      final String resp = Utils.getFromUrl(url, retries, delay);

      if (resp.length() > 0) {

        if (Series.dBuilder == null) {
          Series.dBuilder = Series.dbFactory.newDocumentBuilder();
        }

        final Document doc = Series.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");

        // will be 1000 or less
        totalProcessed = nResp.getLength();

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
            final Element eElement = (Element) nodeResp;

            final Series ser = new Series();
            ser.setUrl(url);

            ser.setId(eElement.getAttribute("id").trim());
            ser.setTitle(eElement.getAttribute("title").trim());
            ser.setFrequency(eElement.getAttribute("frequency").trim());
            ser.setUnits(eElement.getAttribute("units").trim());
            ser.setType(ResponseType.LIN);
            ser.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment").trim());
            ser.setSeasonalAdjustmentShort(eElement.getAttribute("seasonal_adjustment_short").trim());
            ser.setLastUpdate(eElement.getAttribute("last_updated").trim());
            ser.setFirstObservation(eElement.getAttribute("observation_start").trim());
            ser.setLastObservation(eElement.getAttribute("observation_end").trim());

            ser.notes = eElement.getAttribute("notes").trim();

            final boolean newSer = Series.uniqSeries.add(ser.id);
            if (newSer) {

              ser.setValid(true);
              Debug.LOGGER.info(String.format("Adding series data.%n%s", ser));

              Series.seriesList.add(ser);
            }
            else {
              /**
               * This id has already been read. Decrement totalProcess (now less than 1000).
               * Calling process will know query response contained duplicated data and there
               * is nothing new in the FRED database.
               */
              totalProcessed--;
              Debug.LOGGER.info(String.format("Duplicate series not added!%s", ser.getId()));
            }
          }
        }
      }
      else {
        Debug.LOGGER.info(String.format("Warning. No response data returned from FRED for ReleaseId=%s  offset=%d", release_id, offset));
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    Debug.LOGGER.info(String.format("Returning from fredSeriesPerSeriesQuery - totalProcessed=%d", totalProcessed));
    return totalProcessed;
  }

  private String                  url;
  private String                  id;
  private String                  title;
  private String                  frequency;
  private String                  units;
  private String                  firstObservation;
  private String                  lastObservation;
  private DateTime                lastUpdate;
  private String                  seasonalAdjustment;
  private String                  seasonalAdjustmentShort;
  private String                  notes;
  private final String            releaseId;
  private boolean                 valid;
  private DataSeries.ResponseType type;

  public Series() {

    this.url = "";
    this.id = "";
    this.title = "";
    this.frequency = "";
    this.firstObservation = null;
    this.lastObservation = null;
    this.lastUpdate = null;
    this.seasonalAdjustment = "";
    this.seasonalAdjustmentShort = "";
    this.units = "";
    this.notes = "";
    this.releaseId = "";

    this.valid = false;
  }

  public String getFirstObservation() {
    return this.firstObservation;
  }

  public String getFrequency() {
    return this.frequency;
  }

  public String getId() {
    return this.id;
  }

  public String getLastObservation() {
    return this.lastObservation;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  public String getNotes() {
    return this.notes;
  }

  public String getReleaseName() {
    return this.releaseId;
  }

  public String getSeasonalAdjustment() {
    return this.seasonalAdjustment;
  }

  public String getSeasonalAdjustmentShort() {
    return this.seasonalAdjustmentShort;
  }

  public String getTitle() {
    return this.title;
  }

  public DataSeries.ResponseType getType() {
    return this.type;
  }

  public String getUnits() {
    return this.units;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {

    String ret = Utils.NL;
    ret += "Id                : " + this.id + Utils.NL;
    ret += " Title             : " + this.title + Utils.NL;
    ret += " Frequency         : " + this.frequency + Utils.NL;
    ret += " Units             : " + this.units + Utils.NL;
    ret += " Adjustment        : " + this.seasonalAdjustment + Utils.NL;
    ret += " Adjustment S      : " + this.seasonalAdjustmentShort + Utils.NL;
    ret += " Type              : " + this.type + Utils.NL;
    ret += " Notes             : " + this.notes + Utils.NL;
    if (this.lastUpdate != null) {
      ret += " Last Update       : " + this.lastUpdate + Utils.NL;
    }
    if (this.firstObservation != null) {
      ret += " First Observation : " + this.firstObservation + Utils.NL;
    }
    if (this.lastObservation != null) {
      ret += " Last Observation  : " + this.lastObservation + Utils.NL;
    }
    ret += Utils.NL;
    ret += this.url + Utils.NL;
    return ret;
  }

  /**
   *
   * @param dateTimeStr
   */
  void setFirstObservation(String dateTimeStr) {
    this.firstObservation = dateTimeStr;
  }

  void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  void setId(String id) {
    this.id = id;
  }

  /**
   *
   * @param dateTimeStr
   */
  void setLastObservation(String dateTimeStr) {
    this.lastObservation = dateTimeStr;
  }

  void setLastUpdate(DateTime dt) {
    this.lastUpdate = new DateTime(dt);
  }

  /**
   *
   * @param dateTimeStr
   */
  void setLastUpdate(String dateTimeStr) {
    final String tmp = dateTimeStr.substring(0, 19);
    this.lastUpdate = new DateTime(tmp, "yyyy-MM-dd hh:mm:ss");
  }

  void setNotes(String note) {
    this.notes = note;
  }

  void setSeasonalAdjustment(String seasonalAdjustment) {
    this.seasonalAdjustment = seasonalAdjustment;
  }

  void setSeasonalAdjustmentShort(String seasonalAdjustmentShort) {
    this.seasonalAdjustmentShort = seasonalAdjustmentShort;
  }

  void setTitle(String title) {
    this.title = title;
  }

  void setType(DataSeries.ResponseType type) {
    this.type = type;
  }

  void setUnits(String units) {
    this.units = units;
  }

  void setUrl(String url) {
    this.url = url;
  }

  void setValid(boolean valid) {
    this.valid = valid;
  }

}
