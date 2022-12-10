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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import net.ajaskey.market.tools.fred.FredUtils;

public class Series {

  public final static SimpleDateFormat        sdf       = new SimpleDateFormat("yyyy-MM-dd");
  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  private static List<Series> seriesList = new ArrayList<>();
  private static Set<String>  uniqSeries = new HashSet<>();

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
  public static List<Series> querySeriesPerRelease(String release_id, int retries, int delay, String fredlib) {

    Series.seriesList.clear();

    int offset = 0;
    boolean readmore = true;
    while (readmore) {
      Debug.LOGGER.info(String.format("%n---------%ncalling fredSeriesPerSeriesQuery.  release_id=%s  offset=%d", release_id, offset));
      final int num = Series.fredSeriesPerReleaseQuery(release_id, offset, retries, delay, fredlib);
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
   * 
   * @param id
   * @param retries
   * @param delay
   * @param fredlib
   * @return
   */
  public static Series query(String id, int retries, int delay, String fredlib) {

    Series ser = new Series();

    try {

      // https://api.stlouisfed.org/fred/series?series_id=GNPCA&api_key=

      // URL to query a list of series associated with the release_id parameter
      final String url = String.format("https://api.stlouisfed.org/fred/series?series_id=%s&api_key=%s", id, ApiKey.get());

      // XML representing the series data
      final String resp = Utils.getFromUrl(url, retries, delay);

      ser = processResponse(resp, url, fredlib);

    }
    catch (final Exception e) {
      e.printStackTrace();
      ser.setValid(false);
    }

    Debug.LOGGER.info(String.format("Returning from query."));

    return ser;
  }

  /**
   * 
   * @param response
   * @param url
   * @param fredlib
   * @return
   * @throws SAXException
   * @throws IOException
   * @throws ParserConfigurationException
   */
  private static Series processResponse(String response, String url, String fredlib) throws SAXException, IOException, ParserConfigurationException {

    Series ser = new Series();

    if (response.length() > 0) {

      if (Series.dBuilder == null) {
        Series.dBuilder = Series.dbFactory.newDocumentBuilder();
      }

      final Document doc = dBuilder.parse(new InputSource(new StringReader(response)));

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
          ser.setFileDate(null);
          if (ser.title.length() > 0) {
            ser.setFullfilename(FredUtils.toFullFileName(ser.id, ser.title));
          }
          ser.notes = eElement.getAttribute("notes").trim();

          final boolean newSer = Series.uniqSeries.add(ser.id);
          if (newSer) {

            try {
              String fname = String.format("%s/%s.csv", fredlib, ser.getId());
              File f = new File(fname);
              if (f.exists()) {
                ser.setLocalfile(fname);
                ser.setLocalfiledate(new DateTime(f.lastModified()));
              }
              else {
                ser.setLocalfile("");
                ser.setLocalfiledate(null);
              }
            }
            catch (Exception e) {
              ser.setLocalfile("");
              ser.setLocalfiledate(null);
              e.printStackTrace();
            }

            ser.setValid(true);
            Debug.LOGGER.info(String.format("Adding series data. %s", ser.getId()));

            Series.seriesList.add(ser);
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
   * Experiements have found the FRED often returns two blocks of 1000 data items
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
   * @return
   */
  private static int fredSeriesPerReleaseQuery(String release_id, int offset, int retries, int delay, String fredlib) {

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
            ser.setFileDate(null);
            if (ser.title.length() > 0) {
              ser.setFullfilename(FredUtils.toFullFileName(ser.id, ser.title));
            }
            ser.notes = eElement.getAttribute("notes").trim();

            final boolean newSer = Series.uniqSeries.add(ser.id);
            if (newSer) {

              try {
                String fname = String.format("%s/%s.csv", fredlib, ser.getId());
                File f = new File(fname);
                if (f.exists()) {
                  ser.setLocalfile(fname);
                  ser.setLocalfiledate(new DateTime(f.lastModified()));
                }
                else {
                  ser.setLocalfile("");
                  ser.setLocalfiledate(null);
                }
              }
              catch (Exception e) {
                ser.setLocalfile("");
                ser.setLocalfiledate(null);
                e.printStackTrace();
              }

              ser.setValid(true);
              Debug.LOGGER.info(String.format("Adding series data. %s", ser.getId()));

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
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    Debug.LOGGER.info(String.format("Returning from fredSeriesPerSeriesQuery - totalProcessed=%d", totalProcessed));
    return totalProcessed;
  }

  private String   url;
  private String   id;
  private String   title;
  private String   frequency;
  private DateTime firstObservation;
  private DateTime lastObservation;
  private DateTime lastUpdate;
  private String   fullfilename;
  private String   seasonalAdjustment;
  private String   seasonalAdjustmentShort;
  private String   units;
  private String   notes;
  private String   releaseId;
  private String   localfile;
  private DateTime localfiledate;
  private boolean  valid;

  private DataSeries.ResponseType type;

  public Series() {
    this.valid = false;
  }

  public DateTime getFirstObservation() {
    return this.firstObservation;
  }

  public String getFrequency() {
    return this.frequency;
  }

  public String getFullfilename() {
    return this.fullfilename;
  }

  public String getId() {
    return this.id;
  }

  public DateTime getLastObservation() {
    return this.lastObservation;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  public String getLocalfile() {
    return this.localfile;
  }

  public DateTime getLocalfiledate() {
    return this.localfiledate;
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

  public void setFileDate(String fredLib) {
    final String fname = String.format("%s/%s.csv", fredLib, this.getId());
    final File f = new File(fname);
    if (f.exists()) {
      this.localfiledate = new DateTime(f.lastModified());
    }
    else {
      this.localfiledate = null;
    }
  }

  public void setLocalfile(String localfile) {
    this.localfile = localfile;
  }

  public void setLocalfiledate(DateTime localfiledate) {
    this.localfiledate = localfiledate;
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
    ret += " Full filename     : " + this.fullfilename + Utils.NL;
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
    if (this.localfiledate != null) {
      ret += " Local File        : " + this.localfile + "   " + this.localfiledate.toFullString();
    }
    else {
      ret += " File Date         : " + "File not found.";
    }
    ret += Utils.NL;
    ret += this.url + Utils.NL;
    return ret;
  }

  void setFirstObservation(String dateTimeStr) {
    Date d;
    try {
      d = Series.sdf.parse(dateTimeStr);
      this.firstObservation = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  void setFullfilename(String fullfilename) {
    this.fullfilename = fullfilename;
  }

  void setId(String id) {
    this.id = id;
  }

  void setLastObservation(String dateTimeStr) {
    Date d;
    try {
      d = Series.sdf.parse(dateTimeStr);
      this.lastObservation = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  void setLastUpdate(String dateTimeStr) {
    Date d;
    try {
      d = Series.sdf.parse(dateTimeStr);
      this.lastUpdate = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
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
