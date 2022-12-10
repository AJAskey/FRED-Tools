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

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries.ResponseType;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.FredUtils;

public class RecentUpdates {

  private static List<Series>                 updateList  = new ArrayList<>();
  private static Set<String>                  uniqUpdates = new HashSet<>();
  private final static DocumentBuilderFactory dbFactory   = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder    = null;

  private static List<DataSeriesInfo> bigList = null;

  public static void main(String[] args) {

    Debug.init("debug/QueryRecentUpdates.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    List<Series> upList = queryRecentUpdates("FredLib");

    String dbg = Utils.NL + "All Releases" + Utils.NL;
    for (Series upd : upList) {
      Debug.LOGGER.info(upd.toString());
      dbg += upd.getId() + Utils.NL;
    }
    Debug.LOGGER.info(dbg);
  }

  public static List<Series> queryRecentUpdates(String fredlib) {

    updateList.clear();

    int offset = 0;
    int knt = 0;
    boolean readmore = true;
    while (readmore && knt < 100) {
      final int num = fredUpdateQuery(offset, fredlib);
      knt++;
      System.out.printf("offset=%d  num=%d  size=%d%n", offset, num, updateList.size());
      if (num < 1000) {
        readmore = false;
      }
      else {
        offset += num;
      }
    }

    return updateList;
  }

  private static int fredUpdateQuery(int offset, String fredlib) {
    int totalProcessed = 0;

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/series/updates?api_key=%s&offset=%d", ApiKey.get(), offset);

      final String resp = Utils.getFromUrl(url);

      if (resp.length() > 0) {

        if (dBuilder == null) {
          dBuilder = dbFactory.newDocumentBuilder();
        }

        final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");

        totalProcessed = nResp.getLength();

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            Series ser = new Series();
            ser.setUrl(url);

            ser.setId(eElement.getAttribute("id").trim());
            final boolean newSel = uniqUpdates.add(ser.getId());
            if (!newSel) {
              System.out.println("Here");
            }

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
            if (ser.getTitle().length() > 0) {
              ser.setFullfilename(FredUtils.toFullFileName(ser.getId(), ser.getTitle()));
            }
            ser.setNotes(eElement.getAttribute("notes").trim());

            ser.setLocalfile(String.format("%s/%s.csv", fredlib, ser.getId()));
            File localFile = new File(ser.getLocalfile());

            if (localFile.exists()) {
              ser.setLocalfiledate(new DateTime(localFile.lastModified()));
            }
            else {
              ser.setLocalfiledate(null);
            }

            ser.setValid(newSel);

            if (newSel) {
              // Only save update files on local system
              if (ser.getLocalfiledate() != null) {
                updateList.add(ser);
              }
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

  /**
   * 
   * @param dsi
   * @return
   */
  private static boolean isNewValue(DataSeriesInfo dsi) {
    for (DataSeriesInfo bdsi : bigList) {
      if (dsi.getName().equals(bdsi.getName())) {
        Debug.LOGGER.info(String.format("Repeat Ids %s : %s", dsi.getName(), bdsi.getName()));
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param offset
   * @return
   */
//  public static List<DataSeriesInfo> queryRecentUpdates(int offset) {
//
//    final List<DataSeriesInfo> retList = new ArrayList<>();
//
//    String url = String.format("https://api.stlouisfed.org/fred/series/updates?api_key=%s&filter_value=macro&offset=%d", ApiKey.get(), offset);
//
//    try {
//      if (dBuilder == null) {
//        dBuilder = dbFactory.newDocumentBuilder();
//      }
//
//      final String resp = Utils.getFromUrl(url);
//
//      final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));
//
//      doc.getDocumentElement().normalize();
//
//      final NodeList nResp = doc.getElementsByTagName("series");
//
//      for (int knt = 0; knt < nResp.getLength(); knt++) {
//
//        final Node nodeResp = nResp.item(knt);
//
//        if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
//
//          final DataSeriesInfo dsi = new DataSeriesInfo();
//
//          final Element eElement = (Element) nodeResp;
//
//          final String series = eElement.getAttribute("id");
//          Debug.LOGGER.info("Series : " + series);
//
//          dsi.setName(series.trim());
//          dsi.setTitle(eElement.getAttribute("title").trim());
//          dsi.setFrequency(eElement.getAttribute("frequency").trim());
//          dsi.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment_short").trim());
//          dsi.setUnits(eElement.getAttribute("units").trim());
//          dsi.setType("LIN");
//          dsi.setFirstObservation(eElement.getAttribute("observation_start").trim());
//          dsi.setLastObservation(eElement.getAttribute("observation_end").trim());
//          dsi.setLastUpdate(eElement.getAttribute("last_updated").trim());
//          if (dsi.getTitle().length() > 0) {
//            dsi.setFullFilename(FredUtils.toFullFileName(dsi.getName(), dsi.getTitle()));
//          }
//
//          dsi.setResponse("Many series updated.");
//          dsi.setFileDt(null);
//
//          retList.add(dsi);
//        }
//      }
//    }
//    catch (final Exception e) {
//      e.printStackTrace();
//    }
//
//    return retList;
//  }

}
