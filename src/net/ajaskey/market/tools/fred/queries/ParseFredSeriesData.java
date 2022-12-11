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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.TextUtils;

/**
 *
 * @author Computer
 *
 */
public class ParseFredSeriesData {

  private static DateTime usefulDate = new DateTime(2022, DateTime.SEPTEMBER, 1);

  private static List<String> usefulList = new ArrayList<>();

  private static String fredlib = "FredLib";

  private static SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

  public static void main(String[] args) throws FileNotFoundException {

    final List<String> relList = ParseFredSeriesData.getReleasesToProcess("FredSeries/release-list.txt");

    ParseFredSeriesData.setUsefulList();

    final List<ParseFredSeriesData> pdsList = new ArrayList<>();

    try (PrintWriter pwAll = new PrintWriter("out/filteredSeriesSummary.txt")) {

      int processed = 0;

      /**
       * Process each Release from list.
       */
      for (final String fname : relList) {

        System.out.printf("release : %s", fname);

        final String fileToProcess = "FredSeries/" + fname;
        final List<String> data = TextUtils.readTextFile(fileToProcess, false);

        final String header = data.get(0).trim();

        final String filename = header.replaceAll(" : ", "").replaceAll("\"", "");
        /**
         * Files to be read into Optuma as a list of charts.
         */
        try (PrintWriter pw = new PrintWriter("optuma/" + filename + ".csv")) {

          for (int i = 1; i < data.size(); i++) {

            final String s = data.get(i);

            final ParseFredSeriesData pds = new ParseFredSeriesData(s, header);

            if (pds.isValid()) {

              if (pds.isUseful()) {
                pdsList.add(pds);
                pw.printf("%s,%s,%s,%s%n", pds.getName(), pds.getSeasonality(), pds.getFrequency(), pds.getTitle());

                final String sDate = pds.lastUpdate.format("dd-MMM-yyyy");

                processed++;
                pwAll.printf("%-30s %-4s  %-11s  %-30s %-120s %s%n", pds.getName(), pds.getSeasonality(), sDate, pds.getFrequency(), pds.getTitle(),
                    data.get(0).trim());
              }
            }
          }
        }
        System.out.printf("   %d%n", processed);
        processed = 0;
      }
    }

    try (PrintWriter pw = new PrintWriter("debug/ParseFredSeriesData.dbg")) {
      for (final ParseFredSeriesData pds : pdsList) {
        pw.println(pds);
      }
    }
    System.out.println(pdsList.size());
  }

  /**
   *
   * @param filename
   * @return
   */
  private static List<String> getReleasesToProcess(String filename) {
    final List<String> list = TextUtils.readTextFile(filename, false);
    return list;
  }

  private static void setUsefulList() {

    final List<String> useThese = TextUtils.readTextFile("FredSeries/SeriesIdAdditions.txt", false);
    for (final String s : useThese) {
      ParseFredSeriesData.usefulList.add(s.trim());
    }
  }

  private String   name;
  private String   title;
  private String   seasonality;
  private String   frequency;
  private DateTime lastUpdate;
  String           lastUpdateStr;
  private String   release;
  private File     localFile;
  private DateTime localFileDate;
  private boolean  valid;

  public ParseFredSeriesData(String data, String rel) {
    final int len = data.length();
    if (len > 180) {

      String stmp = data.substring(0, 30);
      this.name = stmp.trim();
      stmp = data.substring(30, 151);
      this.title = stmp.trim();
      stmp = data.substring(151, 155);
      this.seasonality = stmp.trim();
      stmp = data.substring(156, 167);
      this.lastUpdateStr = stmp;
      this.lastUpdate = new DateTime(stmp.trim(), this.sdf);
      stmp = data.substring(168);
      this.frequency = stmp.trim();
      this.release = rel;

      this.localFile = new File(String.format("%s/%s.csv", fredlib, this.name));

      if (this.localFile.exists()) {
        this.localFileDate = new DateTime(this.localFile.lastModified());
      }
      else {
        this.localFile = null;
        this.localFileDate = null;
      }

      this.valid = true;
    }
    else {
      this.valid = false;
    }
  }

  public String getFrequency() {
    return this.frequency;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  public String getName() {
    return this.name;
  }

  public String getSeasonality() {
    return this.seasonality;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += String.format("Name          : %s%n", this.name);
    ret += String.format("  Title       : %s%n", this.title);
    ret += String.format("  Seasonality : %s%n", this.seasonality);
    ret += String.format("  Frequency   : %s%n", this.frequency);
    ret += String.format("  Last Update : %s     %s%n", this.lastUpdate, this.lastUpdateStr);
    ret += String.format("  Release     : %s%n", this.release);
    if (this.localFile != null) {
      ret += String.format("  Local File  : %s   %s", this.localFile.getAbsoluteFile(), this.localFileDate);
    }

    return ret;
  }

  private boolean isInUsefulList(String n) {
    for (final String s : ParseFredSeriesData.usefulList) {
      if (s.equalsIgnoreCase(n)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This routine is specific to data I (Andy Askey) want to see. It is a
   * brute-force method.
   * 
   * I suggest that you write your own isUseful function.
   *
   * @return
   */
  private boolean isUseful() {

    if (this.isInUsefulList(this.name)) {
      return true;
    }

    if (this.title.toUpperCase().contains("DISCONTINUED") || this.lastUpdate.isLessThan(ParseFredSeriesData.usefulDate)) {
      return false;
    }

    // Add in Industrial Production NSA Monthly
    if (this.name.startsWith("IP")) {
      if (this.frequency.equals("Monthly")) {
        if (this.seasonality.equals("NSA")) {
          return true;
        }
        else {
          return false;
        }
      }
      else {
        return false;
      }
    }

    // Filter Employment Situation
    if (this.release.contains("Employment Situation")) {
      if (this.title.startsWith("All Employees")) {
        if (this.seasonality.equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    // Add GDPNow Release series
    if (this.release.contains("GDPNow")) {
      return true;
    }

    // Id : 14 G.19 Consumer Credit
    if (this.release.contains("G.19 Consumer Credit")) {
      if (this.name.startsWith("REV")) {
        return true;
      }
      else if (this.name.startsWith("NREV")) {
        return true;
      }
      else if (this.name.startsWith("TOTAL")) {
        return true;
      }

      return false;
    }

    // Filter Id : 51 International Trade in Goods and Services. Desired are in
    // useful list.
    if (this.release.contains("International Trade in Goods and Services")) {
      return false;
    }

    // Filter Id : 53 Gross Domestic Product. Desired are in useful list.
    if (this.release.contains("Gross Domestic Product")) {
      return false;
    }

    // Filter Id : 20 H.4.1. Desired are in useful list.
    if (this.release.contains("H.4.1")) {
      return false;
    }

    // Filter Id : 22 H.4.1. Desired are in useful list.
    if (this.release.contains("H.8 Assets and Liabilities")) {
      return false;
    }

    // Filter Id : 86 Commercial Paper. Desired are in useful list.
    if (this.release.contains("Commercial Paper")) {
      return false;
    }

    // Filter Id : 183 Gasoline and Diesel Fuel Update. Only want basic weekly data.
    if (this.release.contains("Gasoline and Diesel Fuel Update")) {
      if (!this.title.contains("PADD ")) {
        if (this.frequency.contains("Weekly")) {
          return true;
        }

      }
      return false;
    }

    // Filter Id : 321 Empire State Manufacturing Survey. Desired are in useful
    // list.
    if (this.release.contains("Empire State Manufacturing Survey")) {
      return false;
    }

    // Filter Id : 374 Texas Manufacturing Outlook Survey.
    if (this.release.contains("Texas Manufacturing Outlook Survey")) {
      if (this.title.contains("Diffusion Index")) {
        if (this.seasonality.equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    // Filter Id : 377 Texas Retail Outlook Survey.
    if (this.release.contains("Texas Retail Outlook Survey")) {
      if (this.title.contains("Diffusion Index")) {
        if (this.seasonality.equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    // Add in Capacity Utilization SA Monthly
    if (this.release.contains("Industrial Production and Capacity Utilization")) {
      if (this.name.startsWith("CAP")) {
        if (this.frequency.equals("Monthly")) {
          return true;
        }
      }
    }

    // Filter Id : 352 Nonmanufacturing Business Outlook Survey. Desired are in
    // useful list.
    if (this.release.contains("Nonmanufacturing Business Outlook Survey")) {
      return false;
    }

    // Filter Id : 205 Main Economic Indicators. Desired are in
    // useful list.
    if (this.release.contains("Main Economic Indicators")) {
      return false;
    }

    // Filter Id : 46 Producer Price Index.
    if (this.release.contains("Producer Price Index")) {
      if (this.name.length() == 9) {
        long count = this.title.chars().filter(ch -> ch == ':').count();
        if (count < 2) {
          return true;
        }
      }
      return false;
    }

    /**
     * Big ass IF filter developed by trial and error.
     */
    if (!this.seasonality.equalsIgnoreCase("SA")) {
      if (!this.seasonality.equalsIgnoreCase("SAAR")) {
        if (!this.frequency.equalsIgnoreCase("Not Applicable")) {
          if (!this.frequency.contains("Annual")) {
            if (!this.title.toLowerCase().contains("northeast")) {
              if (!this.title.toLowerCase().contains("midwest")) {
                if (!this.title.toLowerCase().contains("south census")) {
                  if (!this.title.toLowerCase().contains("west census")) {
                    if (!this.title.toLowerCase().contains("central census")) {
                      if (!this.title.toLowerCase().contains("atlantic census")) {
                        if (!this.title.toLowerCase().contains("mountain census")) {
                          if (!this.title.toLowerCase().contains("pacific census")) {
                            if (!this.title.toLowerCase().contains("england census")) {
                              if (!this.title.contains("Establishments")) {
                                if (!this.title.contains("Employment-Population Ratio -")) {
                                  if (!this.title.contains("mployed -")) {
                                    if (!this.title.contains("Not in Labor Force -")) {
                                      if (!this.title.contains("mployment Rate -")) {
                                        if (!this.title.contains("mployment Rate:")) {
                                          if (!this.title.contains("Civilian Labor Force ")) {
                                            if (!this.title.contains("mployment Level -")) {
                                              if (!this.title.contains("Civilian Labor Force:")) {
                                                if (!this.title.contains("mployment Level:")) {
                                                  if (!this.title.contains("Population Level -")) {
                                                    if (!this.title.contains("Labor Force Participation Rate -")) {
                                                      if (!this.title.contains("Houses Sold by ")) {
                                                        if (!this.title.contains("Multiple Jobholders")) {

                                                          return true;
                                                        }
                                                      }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

}
