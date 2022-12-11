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

package net.ajaskey.market.tools.fred;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;

public class DataSeriesInfo {

  public final static SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  public final static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  /**
   * net.ajaskey.market.tools.fred.main
   *
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(final String[] args) throws FileNotFoundException {

    Debug.init("out/dsi.dbg", java.util.logging.Level.INFO);

    final DataSeriesInfo dsi = new DataSeriesInfo("CEU0500000001", new DateTime());

    try (PrintWriter pw = new PrintWriter("out/fred-series.txt")) {
      pw.println("Series\tTitle\tFrequency\tUnits\tSeasonality\tLastUpdate");
      pw.println(dsi.toCsvString());
    }
    catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static List<DataSeriesInfo> readSeriesInfo() {

    final List<DataSeriesInfo> dList = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader("data/fred-series-info.txt"))) {

      String line;
      // Utils.printCalendar(d.getDate());
      while ((line = reader.readLine()) != null) {
        final String str = line.trim();
        if (str.length() > 1) {
          final String s = str.substring(0, 1);
          if (!s.contains("#")) {
            final String fld[] = str.split("\t");
            final DataSeriesInfo dsi = new DataSeriesInfo();
            dsi.name = fld[0].trim().toUpperCase();
            dsi.title = fld[1].trim();
            dsi.units = fld[4].trim();
            dsi.type = DataSeries.ResponseType.valueOf(fld[5].trim());
            dList.add(dsi);
          }
        }

      }
    }
    catch (final IOException e) {
      e.printStackTrace();
    }

    return dList;
  }

  private DateTime                fileDt;
  private String                  frequency;
  private DateTime                firstObservation;
  private DateTime                lastObservation;
  private DateTime                lastUpdate;
  private String                  name;
  private String                  fullfilename;
  private String                  response;
  private String                  seasonalAdjustment;
  private String                  title;
  private DataSeries.ResponseType type;
  private String                  units;
  private boolean                 valid;

  /**
   * This method serves as a constructor for the class.
   *
   */
  public DataSeriesInfo() {

    this.response = "";
    this.name = "";
    this.fullfilename = "";
    this.title = "";
    this.frequency = "";
    this.units = "";
    this.seasonalAdjustment = "";
    this.firstObservation = null;
    this.lastObservation = null;
    this.lastUpdate = null;
    this.fileDt = null;
    this.type = DataSeries.ResponseType.LIN;
    this.valid = false;
  }

  public DataSeriesInfo(final String fld[]) {

    final int len = fld.length;
    this.valid = false;
    this.response = "";
    if (len > 1) {
      this.name = fld[0].trim();
      this.title = fld[1].trim();
      this.frequency = fld[3].trim();
      this.units = fld[4].trim();
      this.setType(fld[5].trim());
      this.firstObservation = null;
      this.lastObservation = null;

      this.fileDt = new DateTime(fld[6].trim(), "dd-MMM-yyyy");
      this.lastUpdate = new DateTime(fld[7].trim(), "dd-MMM-yyyy");

      this.seasonalAdjustment = "";
      this.valid = true;
    }
    else {
      this.name = fld[0];
      this.title = "";
      this.frequency = "";
      this.units = "";
      this.seasonalAdjustment = "";
      this.firstObservation = null;
      this.lastObservation = null;
      this.lastUpdate = null;
      this.fileDt = null;
      this.type = DataSeries.ResponseType.LIN;
      this.valid = true;
    }
  }

  /**
   * This method serves as a constructor for the class.
   *
   * @param lastUpdate
   *
   */
  public DataSeriesInfo(final String seriesName, DateTime fileDt) {

    this.setName(seriesName.trim());

    final String url = "https://api.stlouisfed.org/fred/series?series_id=" + this.name + "&api_key=" + ApiKey.get();

    String resp;
    try {
      if (DataSeriesInfo.dBuilder == null) {
        DataSeriesInfo.dBuilder = DataSeriesInfo.dbFactory.newDocumentBuilder();
      }

      resp = Utils.getFromUrl(url, 6, 15);

      if (resp.length() < 1) {
        return;
      }

      this.response = resp.trim();
      // Debug.LOGGER.info(this.response + Utils.NL);

      boolean found = false;
      this.valid = false;
      if (resp.length() > 0) {

        final Document doc = DataSeriesInfo.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");
        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
            final Element eElement = (Element) nodeResp;

            this.setResponse(resp.trim());
            this.setTitle(eElement.getAttribute("title").trim());
            this.setFrequency(eElement.getAttribute("frequency").trim());
            this.setUnits(eElement.getAttribute("units").trim());
            this.setType("LIN");
            this.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment_short").trim());
            this.setLastUpdate(eElement.getAttribute("last_updated").trim());
            this.setFirstObservation(eElement.getAttribute("observation_start").trim());
            this.setLastObservation(eElement.getAttribute("observation_end").trim());
            this.setFileDt(fileDt);
            if (this.title.length() > 0) {
              found = true;
              this.fullfilename = FredUtils.toFullFileName(this.name, this.title);
            }
          }
        }
        this.valid = found;
      }

    }
    catch (final Exception e) {
      this.setName("");
      this.valid = false;
      e.printStackTrace();
    }
  }

  /**
   * @return the fileDt
   */
  public DateTime getFileDt() {

    return this.fileDt;
  }

  public DateTime getFirstObservation() {

    return this.firstObservation;
  }

  /**
   * @return the frequency
   */
  public String getFrequency() {

    return this.frequency;
  }

  public String getFullfilename() {
    return this.fullfilename;
  }

  /**
   * @return the lastObservation
   */
  public DateTime getLastObservation() {

    return this.lastObservation;
  }

  /**
   * @return the lastUpdate
   */
  public DateTime getLastUpdate() {

    return this.lastUpdate;
  }

  /**
   * @return the name
   */
  public String getName() {

    return this.name;
  }

  /**
   * @return the response
   */
  public String getResponse() {

    return this.response;
  }

  /**
   * @return the seasonalAdjusted
   */
  public String getSeasonalAdjusted() {

    return this.seasonalAdjustment;
  }

  /**
   * @return the title
   */
  public String getTitle() {

    return this.title;
  }

  /**
   * @return the type
   */
  public DataSeries.ResponseType getType() {

    return this.type;
  }

  /**
   * @return the units
   */
  public String getUnits() {

    return this.units;
  }

  public boolean isValid() {
    return this.valid;
  }

  /**
   * @param fileDt the fileDt to set
   */
  public void setFileDt(DateTime fileDt) {

    this.fileDt = fileDt;
  }

  public void setResponse(String r) {
    this.response = r;
  }

  /**
   *
   * @param attribute
   */
  public void setFirstObservation(final String attribute) {

    try {
      final Date d = DataSeriesInfo.sdf2.parse(attribute);
      this.firstObservation = new DateTime(d);

    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param lastObservation the lastObservation to set
   */
  public void setLastObservation(final String attribute) {

    try {
      final Date d = DataSeriesInfo.sdf2.parse(attribute);
      this.lastObservation = new DateTime(d);

    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  public void setLastUpdate(DateTime lastupdate) {
    this.lastUpdate = new DateTime(lastupdate);
  }

  /**
   * @param seasonalAdjusted the seasonalAdjusted to set
   */
  public void setSeasonalAdjustment(final String adjustment) {

    this.seasonalAdjustment = adjustment;
  }

  /**
   * @param type the type to set
   */
  public void setType(final String type) {

    try {
      this.type = DataSeries.ResponseType.valueOf(type);
    }
    catch (final Exception e) {
      this.type = DataSeries.ResponseType.LIN;
    }
  }

  /**
   * @param units the units to set
   */
  public void setUnits(final String units) {

    this.units = units;
  }

  /**
   * @param fullFilename to set
   */
  public void setFullFilename(final String ffn) {

    this.fullfilename = ffn;
  }

  public String toCsvString() {

    final String ret = this.name + Utils.TAB + this.title + Utils.TAB + this.frequency + Utils.TAB + this.units + Utils.TAB + this.seasonalAdjustment
        + Utils.TAB + this.fileDt + Utils.TAB + this.lastUpdate;
    return ret;
  }

  @Override
  public String toString() {

    String ret = Utils.NL + this.response.trim() + Utils.NL;
    ret += "Name                : " + this.name + Utils.NL;
    ret += "  Title             : " + this.title + Utils.NL;
    ret += "  Frequency         : " + this.frequency + Utils.NL;
    ret += "  Units             : " + this.units + Utils.NL;
    ret += "  Adjustment        : " + this.seasonalAdjustment + Utils.NL;
    ret += "  Type              : " + this.type + Utils.NL;
    ret += "  Full filename     : " + this.fullfilename + Utils.NL;
    if (this.lastUpdate != null) {
      ret += "  Last Update       : " + this.lastUpdate.toFullString() + Utils.NL;
    }
    if (this.firstObservation != null) {
      ret += "  First Observation : " + this.firstObservation + Utils.NL;
    }
    if (this.lastObservation != null) {
      ret += "  Last Observation  : " + this.lastObservation + Utils.NL;
    }
    if (this.fileDt != null) {
      ret += "  File Date         : " + this.fileDt.toFullString();
    }
    return ret;
  }

  /**
   * @param frequency the frequency to set
   */
  public void setFrequency(final String frequency) {

    this.frequency = frequency;
  }

  /**
   * net.ajaskey.market.tools.fred.setLastUpdate
   *
   * @param attribute
   */
  public void setLastUpdate(final String attribute) {

    try {
      final int idx = attribute.lastIndexOf("-");
      final String dstr = attribute.substring(0, idx);
      final Date d = DataSeriesInfo.sdf.parse(dstr);
      this.lastUpdate = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }

  }

  /**
   * @param name the name to set
   */
  public void setName(final String name) {

    this.name = name;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(final String title) {

    final String filtered = title.replaceAll("[^\\x00-\\x7F]", " ");
    this.title = filtered.trim();
  }

}
