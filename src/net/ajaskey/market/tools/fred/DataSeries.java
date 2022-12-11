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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

public class DataSeries {

  public enum AggregationMethodType {
    AVG, EOP, SUM
  }

  public enum FileType {
    JSON, TXT, XLS, XML
  }

  public enum OrderType {
    ASC, DESC
  }

  /*
   * lin = Levels (No transformation)
   *
   * chg = Change ch1 = Change from Year Ago
   *
   * pch = Percent Change
   *
   * pc1 = Percent Change from Year Ago
   *
   * pca = Compounded Annual Rate of Change
   *
   * cch = Continuously Compounded Rate of Change
   *
   * cca = Continuously Compounded Annual Rate of Change log = Natural Log
   */
  public enum ResponseType {
    CCA, CCH, CH1, CHG, LIN, LOG, PC1, PCA, PCH
  }

  /**
   * net.ajaskey.market.tools.fred.main
   *
   * @param args
   */
  public static void main(final String[] args) {

    final DataSeries ds = new DataSeries("AMTMUO");

    if (ds.isValid()) {

      ds.setAggType(AggregationMethodType.EOP);
      // ds.setOrder(OrderType.DESC);
      ds.setRespType(ResponseType.LIN);
      final List<DataValues> dvList = ds.getValues(1.0, true, false);

      for (final DataValues d : dvList) {
        System.out.println(d.getDate() + "  " + d.getValue());
      }

      System.out.println(ds);

    }
  }

  private AggregationMethodType        aggType;
  private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private DocumentBuilder              dBuilder  = null;
  private DateTime                     dtOne;
  private FileType                     fileType;
  private DataSeriesInfo               info;
  private int                          limit;
  private String                       name;
  private int                          offset;
  private OrderType                    order;
  private String                       period;
  private int                          respKnt;
  private ResponseType                 respType;
  private boolean                      valid;
  private List<DataValues>             dvList;

  /**
   * This method serves as a constructor for the class.
   *
   * @param dsi
   */
  public DataSeries(DataSeriesInfo dsi) {

    this.valid = false;
    this.info = null;

    if (dsi != null) {
      this.setName(dsi.getName());
      this.setAggType(AggregationMethodType.AVG);
      this.setFileType(FileType.XML);
      this.setLimit(100000);
      this.setOffset(0);
      this.setOrder(OrderType.ASC);
      this.setRespType(ResponseType.LIN);
      this.setRespKnt("0");
      this.setPeriod(dsi.getFrequency());
      this.dtOne = null;
      this.info = dsi;

      try {
        this.dBuilder = this.dbFactory.newDocumentBuilder();
        this.dvList = this.getValues(0.0, false, false);
        if (this.dvList.size() > 0) {
          this.valid = true;
        }
      }
      catch (final ParserConfigurationException e) {
        this.valid = false;
        e.printStackTrace();
      }
    }
  }

  /**
   * This method serves as a constructor for the class.
   *
   */
  public DataSeries(final String name) {

    this.setName(name);
    this.setAggType(AggregationMethodType.AVG);
    this.setFileType(FileType.XML);
    this.setLimit(100000);
    this.setOffset(0);
    this.setOrder(OrderType.ASC);
    this.setRespType(ResponseType.LIN);
    this.setRespKnt("0");

    this.dtOne = null;
    this.dvList = null;
    this.valid = false;

    this.info = new DataSeriesInfo(name, new DateTime());
    this.period = this.info.getFrequency();

    try {
      this.dBuilder = this.dbFactory.newDocumentBuilder();
      this.valid = true;
    }
    catch (final ParserConfigurationException e) {
      this.dBuilder = null;
      this.valid = false;
      e.printStackTrace();
    }
  }

  /**
   * @return the aggType
   */
  public String getAggType() {

    return "&aggregation_type=" + this.aggType.toString().toLowerCase();
  }

  public List<DataValues> getDvList() {
    return this.dvList;
  }

  /**
   * @return the type
   */
  public String getFileType() {

    return "&file_type=" + this.fileType.toString().toLowerCase();
  }

  public DataSeriesInfo getInfo() {

    return this.info;
  }

  /**
   * @return the limit
   */
  public String getLimit() {

    return "&limit=" + this.limit;
  }

  /**
   * @return the name
   */
  public String getName() {

    return this.name;
  }

  /**
   * @return the offset
   */
  public String getOffset() {

    return "&offset=" + this.offset;
  }

  /**
   * @return the order
   */
  public String getOrder() {

    return "&sort_order=" + this.order.toString().toLowerCase();

  }

  /**
   * @return the period
   */
  public String getPeriod() {

    return this.period;
  }

  /**
   * @return the respKnt
   */
  public int getRespKnt() {

    return this.respKnt;
  }

  /**
   * @return the respType
   */
  public String getRespType() {

    return "&units=" + this.respType.toString().toLowerCase();
  }

  /**
   *
   * net.ajaskey.market.tools.fred.getValues
   *
   * @param futureChg
   * @param noZeroValues
   * @param estimateData
   * @return
   */
  public List<DataValues> getValues(final double futureChg, final boolean noZeroValues, final boolean estimateData) {

    final List<DataValues> retList = new ArrayList<>();

    final String url = "https://api.stlouisfed.org/fred/series/observations?series_id=" + this.name + this.getAggType() + this.getFileType()
        + this.getLimit() + this.getOffset() + this.getOrder() + this.getRespType() + "&api_key=" + ApiKey.get();

    Debug.LOGGER.info(url);

    String resp;
    try {
      resp = Utils.getFromUrl(url, 6, 15);

      if (resp.length() > 0) {

        final Document doc = this.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("observations");
        for (int knt = 0; knt < nResp.getLength(); knt++) {
          final Node nodeResp = nResp.item(knt);
          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
            final Element eElement = (Element) nodeResp;
            this.setRespKnt(eElement.getAttribute("count"));
          }
        }

        final NodeList nList = doc.getElementsByTagName("observation");
        for (int ptr = 0; ptr < nList.getLength(); ptr++) {
          final Node nNode = nList.item(ptr);
          if (nNode.getNodeType() == Node.ELEMENT_NODE) {
            final Element eElement = (Element) nNode;
            final DataValues dv = new DataValues(eElement.getAttribute("date"), eElement.getAttribute("value"));
            final int zeroCheck = (int) (dv.getValue() * 1000.0);

            if (noZeroValues && zeroCheck == 0) {
              this.respKnt -= 1;
            }
            else {
              // System.out.println(Utils.getString(dv.getDate()));
              retList.add(dv);
              // dv.getValue();
              if (this.dtOne == null) {
                this.dtOne = dv.getDate();
              }
            }
          }
        }
      }
    }
    catch (IOException | SAXException e) {
      e.printStackTrace();
    }

    if (estimateData) {
      this.appendEstimates(retList, futureChg);
    }
    else {
      // duplicateLastValue(retList);
    }

    Debug.LOGGER.info(String.format("Values returned : %d", retList.size()));
    return retList;
  }

  public boolean isValid() {

    return this.valid;
  }

  /**
   * @param aggType the aggType to set
   */
  public void setAggType(final AggregationMethodType aggType) {

    this.aggType = aggType;
  }

  /**
   * @param type the type to set
   */
  public void setFileType(final FileType type) {

    this.fileType = type;
  }

  /**
   * @param limit the limit to set
   */
  public void setLimit(final int limit) {

    this.limit = limit;
  }

  /**
   * @param offset the offset to set
   */
  public void setOffset(final int offset) {

    this.offset = offset;
  }

  /**
   * @param order the order to set
   */
  public void setOrder(final OrderType order) {

    this.order = order;
  }

  /**
   * @param period the period to set
   */
  public void setPeriod(final String period) {

    this.period = period.toLowerCase();
  }

  /**
   * @param respKnt the respKnt to set
   */
  public void setRespKnt(final String strRespKnt) {

    try {
      this.respKnt = Integer.parseInt(strRespKnt);
    }
    catch (final Exception e) {
      this.respKnt = 0;
    }
  }

  /**
   * @param respType the respType to set
   */
  public void setRespType(final ResponseType respType) {

    this.respType = respType;
  }

  @Override
  public String toString() {

    String ret;
    try {
      if (this.dvList != null) {
        ret = String.format("Name=%s%n respKnt=%d values=%d%n offset=%d%n order=%s%n period=%s%n limit=%d%n respType=%s%n valid=%s", this.name,
            this.respKnt, this.dvList.size(), this.offset, this.order, this.period, this.limit, this.respType, this.valid);
      }
      else {
        ret = String.format("Warning. dvList is null!", this.name);
      }
    }
    catch (final Exception e) {
      ret = String.format("Warning. Exception converting DataSeries to String!");
      e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.tools.fred.appendEstimates
   *
   * @param retList
   * @param futureChg
   */
  private void appendEstimates(final List<DataValues> retList, final double futureChg) {

    this.setPeriod(this.info.getFrequency());

    final DateTime dt = new DateTime();
    final DateTime dtLast = new DateTime(retList.get(retList.size() - 1).getDate().getCal());

    final double last = retList.get(retList.size() - 1).getValue();

    int periodKnt = 0;
    int duration = 0;
    if (this.period.contains("daily")) {
      periodKnt = 1;
      duration = DateTime.DATE;
    }
    else if (this.period.contains("weekly")) {
      periodKnt = 7;
      duration = DateTime.DATE;
    }
    else if (this.period.contains("monthly")) {
      periodKnt = 1;
      duration = DateTime.MONTH;
    }
    else {
      periodKnt = 3;
      duration = DateTime.MONTH;
    }

    dtLast.add(duration, periodKnt);

    final double val = last + last * (futureChg / 100.0);
    final DataValues dv = new DataValues(dtLast, val);
    retList.add(dv);

    final DateTime tmp = new DateTime(dtLast.getCal());
    while (tmp.isLessThan(dt)) {
      final DateTime nCal = new DateTime(tmp.getCal());
      nCal.add(duration, periodKnt);
      final DataValues dv1 = new DataValues(nCal, val);
      retList.add(dv1);
      tmp.set(nCal.getCal().get(DateTime.YEAR), nCal.getCal().get(DateTime.MONTH), nCal.getCal().get(DateTime.DATE));
    }

  }

  /**
   * @param name the name to set
   */
  private void setName(final String name) {

    this.name = name;
  }

}
