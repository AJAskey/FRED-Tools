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
package net.ajaskey.market.tools.fred.executables;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.TextUtils;
import net.ajaskey.market.tools.fred.LocalFormat;
import net.ajaskey.market.tools.fred.queries.Series;

/**
 *
 * The <b>PullAll</b> class queries for all FRED Releases and the associated
 * Series descriptions.
 * 
 * This class parses that data and creates files for external use.
 * 
 * This class is an example of one way to process the data. Every user will find
 * specific ways to process the data to meet their needs.
 *
 */
public class ParseFredSeriesData {

  private static DateTime usefulDate = new DateTime(2022, DateTime.SEPTEMBER, 1);

  private static List<String> usefulList = new ArrayList<>();

  private static String fredlib = "FredLib";

  /**
   * Main processing procedure.
   * 
   * This procedure is an example of what can be done. It is not required if you
   * don't like. Write your own.
   * 
   * @param args None expected at this time.
   */
  public static void main(String[] args) {

    final List<String> relList = ParseFredSeriesData.getReleasesToProcess("FredSeries/release-list.txt");

    ParseFredSeriesData.setUsefulList();

    final List<LocalFormat> lfList = new ArrayList<>();

    try (PrintWriter pwAll = new PrintWriter("out/filteredSeriesSummary.txt")) {

      int processed = 0;

      /**
       * Process each Release from list. The list can be any Releases you want to
       * process. The Release class main() will dump all releases found from FRED as a
       * starting point.
       */
      for (final String fname : relList) {

        System.out.printf("release : %s", fname);

        final String fileToProcess = "FredSeries/" + fname;
        final List<String> data = TextUtils.readTextFile(fileToProcess, false);

        final String header = data.get(0).trim();
        final LocalFormat lf = new LocalFormat(header, ParseFredSeriesData.fredlib);

        /**
         * Files to be read into Optuma as a list of charts.
         */
        try (PrintWriter pw = new PrintWriter("optuma/" + lf.getFilename() + ".csv")) {

          for (int i = 1; i < data.size(); i++) {

            final String s = data.get(i);

            lf.parseline(s);
            if (lf.isValid()) {
              if (ParseFredSeriesData.isUseful(lf)) {
                pw.printf("%s,%s,%s,%s%n", lf.getId(), lf.getSeasonality(), lf.getFrequency(), lf.getTitle());
                String str = lf.formatline();
                str += String.format(" %-3s %-10s", lf.getReleaseId(), lf.getReleaseName());
                pwAll.println(str);
                lfList.add(lf);
                processed++;
              }
            }
          }
        }
        System.out.printf("   %d%n", processed);
        processed = 0;
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    try (PrintWriter pw = new PrintWriter("debug/ParseFredSeriesData.dbg")) {
      for (final LocalFormat lf : lfList) {
        pw.println(lf);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println(lfList.size());
  }

  /**
   * Reads a list of Releases to process.
   *
   * @param filename
   * @return
   */
  private static List<String> getReleasesToProcess(String filename) {
    final List<String> list = TextUtils.readTextFile(filename, false);
    return list;
  }

  /**
   * Checks Series id against a list to process if found.
   * 
   * @param n
   * @return
   */
  private static boolean isInUsefulList(String n) {
    for (final String s : ParseFredSeriesData.usefulList) {
      if (s.equalsIgnoreCase(n)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This routine is specific to data I (Andy Askey) want to see. It is a
   * brute-force filter method.
   *
   * I suggest that you write your own isUseful function.
   *
   * @return
   */
  private static boolean isUseful(LocalFormat lfdata) {

    if (ParseFredSeriesData.isInUsefulList(lfdata.getId())) {
      return true;
    }

    if (lfdata.getTitle().toUpperCase().contains("DISCONTINUED") || lfdata.getTitle().toUpperCase().contains("(DISC")
        || lfdata.getLastUpdate().isLessThan(ParseFredSeriesData.usefulDate)) {
      return false;
    }

    // Add in Industrial Production NSA Monthly
    if (lfdata.getReleaseName().contains("Industrial Production and Capacity Utilization")) {
      if (lfdata.getId().startsWith("IP")) {
        if (lfdata.getFrequency().equals("Monthly")) {
          if (lfdata.getSeasonality().equals("NSA")) {
            final long count = lfdata.getTitle().chars().filter(ch -> ch == ':').count();
            if (count < 2) {
              return true;
            }
          }
        }
        return false;
      }
    }

    // Add in Capacity Utilization SA Monthly
    if (lfdata.getReleaseName().contains("Industrial Production and Capacity Utilization")) {
      if (lfdata.getId().startsWith("CAP")) {
        if (lfdata.getFrequency().equals("Monthly")) {
          final long count = lfdata.getTitle().chars().filter(ch -> ch == ':').count();
          if (count < 2) {
            return true;
          }
        }
      }
      return false;
    }

    // Filter Employment Situation
    if (lfdata.getReleaseName().contains("Employment Situation")) {
      if (lfdata.getTitle().startsWith("All Employees")) {
        if (lfdata.getSeasonality().equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    // Add GDPNow Release series
    if (lfdata.getReleaseName().contains("GDPNow")) {
      return true;
    }

    // Id : 14 G.19 Consumer Credit
    if (lfdata.getReleaseName().contains("G.19 Consumer Credit")) {
      if (lfdata.getId().startsWith("REV")) {
        return true;
      }
      else if (lfdata.getId().startsWith("NREV")) {
        return true;
      }
      else if (lfdata.getId().startsWith("TOTAL")) {
        return true;
      }

      return false;
    }

    // Filter Id : 20 H.4.1 Desired are in useful list.
    // Filter Id : 22 H.8 Desired are in useful list.
    if (lfdata.getReleaseName().contains("H.4.1") || lfdata.getReleaseName().contains("H.8 Assets and Liabilities")) {
      return false;
    }

    // Filter Id : 46 Producer Price Index.
    if (lfdata.getReleaseName().contains("Producer Price Index")) {
      if (lfdata.getId().length() == 9) {
        final long count = lfdata.getTitle().chars().filter(ch -> ch == ':').count();
        if (count < 2) {
          return true;
        }
      }
      return false;
    }

    // Filter Id : 51 International Trade in Goods and Services. Desired are in
    // useful list.
    if (lfdata.getReleaseName().contains("International Trade in Goods and Services")) {
      return false;
    }

    // Filter Id : 52 Z.1 Financial Accounts of the United States. Desired are in
    // useful list.
    if (lfdata.getReleaseName().contains("Z.1 Financial Accounts of the United States")) {
      return false;
    }

    // Filter Id : 53 Gross Domestic Product. Desired are in useful list.
    if (lfdata.getReleaseName().contains("Gross Domestic Product")) {
      return false;
    }

    // Filter Id : 86 Commercial Paper. Desired are in useful list.
    if (lfdata.getReleaseName().contains("Commercial Paper")) {
      return false;
    }

    // Filter Id : 183 Gasoline and Diesel Fuel Update. Only want basic weekly data.
    if (lfdata.getReleaseName().contains("Gasoline and Diesel Fuel Update")) {
      if (!lfdata.getTitle().contains("PADD ")) {
        if (lfdata.getFrequency().contains("Weekly")) {
          return true;
        }

      }
      return false;
    }

    // Filter Id : 205 Main Economic Indicators. Desired are in
    // useful list.
    if (lfdata.getReleaseName().contains("Main Economic Indicators")) {
      return false;
    }

    // Filter Id : 321 Empire State Manufacturing Survey. Desired are in useful
    // list.
    if (lfdata.getReleaseName().contains("Empire State Manufacturing Survey")) {
      return false;
    }

    // Filter Id : 322 Business Leaders Survey.
    if (lfdata.getReleaseName().contains("Business Leaders Survey")) {
      if (lfdata.getTitle().contains("Diffusion Index for FRB")) {
        return true;
      }
      return false;
    }

    // Filter Id : 352 Nonmanufacturing Business Outlook Survey. Desired are in
    // useful list.
    if (lfdata.getReleaseName().contains("Nonmanufacturing Business Outlook Survey")) {
      return false;
    }

    // Filter Id : 374 Texas Manufacturing Outlook Survey.
    if (lfdata.getReleaseName().contains("Texas Manufacturing Outlook Survey")) {
      if (lfdata.getTitle().contains("Diffusion Index")) {
        if (lfdata.getSeasonality().equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    // Filter Id : 377 Texas Retail Outlook Survey.
    if (lfdata.getReleaseName().contains("Texas Retail Outlook Survey")) {
      if (lfdata.getTitle().contains("Diffusion Index")) {
        if (lfdata.getSeasonality().equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    /**
     * Big ass IF filter developed by trial and error.
     */
    if (!lfdata.getSeasonality().equalsIgnoreCase("SA")) {
      if (!lfdata.getSeasonality().equalsIgnoreCase("SAAR")) {
        if (!lfdata.getFrequency().equalsIgnoreCase("Not Applicable")) {
          if (!lfdata.getFrequency().contains("Annual")) {
            if (!lfdata.getTitle().toLowerCase().contains("northeast")) {
              if (!lfdata.getTitle().toLowerCase().contains("midwest")) {
                if (!lfdata.getTitle().toLowerCase().contains("south census")) {
                  if (!lfdata.getTitle().toLowerCase().contains("west census")) {
                    if (!lfdata.getTitle().toLowerCase().contains("central census")) {
                      if (!lfdata.getTitle().toLowerCase().contains("atlantic census")) {
                        if (!lfdata.getTitle().toLowerCase().contains("mountain census")) {
                          if (!lfdata.getTitle().toLowerCase().contains("pacific census")) {
                            if (!lfdata.getTitle().toLowerCase().contains("england census")) {
                              if (!lfdata.getTitle().contains("Establishments")) {
                                if (!lfdata.getTitle().contains("Employment-Population Ratio -")) {
                                  if (!lfdata.getTitle().contains("mployed -")) {
                                    if (!lfdata.getTitle().contains("Not in Labor Force -")) {
                                      if (!lfdata.getTitle().contains("mployment Rate -")) {
                                        if (!lfdata.getTitle().contains("mployment Rate:")) {
                                          if (!lfdata.getTitle().contains("Civilian Labor Force ")) {
                                            if (!lfdata.getTitle().contains("mployment Level -")) {
                                              if (!lfdata.getTitle().contains("Civilian Labor Force:")) {
                                                if (!lfdata.getTitle().contains("mployment Level:")) {
                                                  if (!lfdata.getTitle().contains("Population Level -")) {
                                                    if (!lfdata.getTitle().contains("Labor Force Participation Rate -")) {
                                                      if (!lfdata.getTitle().contains("Houses Sold by ")) {
                                                        if (!lfdata.getTitle().contains("Multiple Jobholders")) {

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

  /**
   * 
   */
  private static void setUsefulList() {

    final List<String> useThese = TextUtils.readTextFile("FredSeries/SeriesIdAdditions.txt", false);
    for (final String s : useThese) {
      ParseFredSeriesData.usefulList.add(s.trim());
    }
  }

  private Series      series;
  private LocalFormat data;
  private String      release;
  private boolean     valid;

  public LocalFormat getData() {
    return this.data;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += String.format("Id          : %s%n", this.series.getId());
    ret += String.format("  Title       : %s%n", this.series.getTitle());
    ret += String.format("  Seasonality : %s%n", this.series.getSeasonalAdjustmentShort());
    ret += String.format("  Frequency   : %s%n", this.series.getFrequency());
    ret += String.format("  Last Update : %s%n", this.series.getLastUpdate());
    ret += String.format("  Release     : %s%n", this.release);

    return ret;
  }

}
