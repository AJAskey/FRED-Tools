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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

/**
 * 
 * Class is designed to query FRED for a list of all Releases. Then it queries
 * FRED about every Series in each Release.
 * 
 * Files are output for use in subsequent processing tools. The
 * <b>LastUpdate</b> field is very useful.
 *
 */
public class PullAll {

  static public String seriesOutputFormat = "%-40s%-120s %-4s %-12s %s";

  static List<String> summary = new ArrayList<>();

  static PrintWriter allSeriesPw = null;

  /**
   * 
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {

    Debug.init("debug/PullAll.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    processAll("FredSeries", "FredLib", 6, 7);
  }

  /**
   * 
   * @param fredlib
   * @throws FileNotFoundException
   */
  public static void processAll(String serieslib, String fredlib, int retries, int delay) throws FileNotFoundException {

    allSeriesPw = new PrintWriter("out/allSeriesSummary.txt");

    List<Release> relList = Release.queryReleases();

    int num = process(relList, retries, delay, serieslib, fredlib);

    System.out.printf("processed = %d%n", num);

    allSeriesPw.close();
    Collections.sort(summary);
    try (PrintWriter pw = new PrintWriter("out/FRED-Release-Summary.txt")) {
      pw.println("Id\tName\tSeries Count");
      for (String s : summary) {
        pw.println(s);
      }
    }
  }

  /**
   * 
   * @param relList
   * @param retries
   * @param delay
   * @param fredlib
   * @return
   * @throws FileNotFoundException
   */
  private static int process(List<Release> relList, int retries, int delay, String seriesLib, String fredlib) throws FileNotFoundException {

    int totalProcessed = 0;

    for (Release rel : relList) {

      String cleanname = rel.getName().replaceAll("/", "").replaceAll(":", "").replaceAll("\\.", "_").replaceAll("\"", "").replaceAll("&", "_");
      String tmp = String.format("%s/Id_%s%s.txt", seriesLib, rel.getId(), cleanname);
      String fn = tmp.replaceAll(" ", "");

      // Remove existing file so new file will show date of creation. Must be a
      // Windows feature.
      File f = new File(fn);
      if (f.exists()) {
        f.delete();
      }

      try (PrintWriter pw = new PrintWriter(fn)) {

        String s = String.format("Id : %s  %s", rel.getId(), rel.getName());
        pw.println(s);

        List<Series> serList = Series.querySeriesPerRelease(rel.getId(), retries, delay, "FredLib");

        System.out.println(String.format("Processed querySeriesPerRelease for %-25s %5d  %-120s %s", rel.getId(), serList.size(), rel.getName(), fn));

        if (serList.size() > 0) {
          for (Series ser : serList) {

            String t = ser.getTitle().trim();
            if (t.length() > 120) {
              t = t.substring(0, 119);
            }

            String sum = String.format(seriesOutputFormat, ser.getId(), t, ser.getSeasonalAdjustmentShort(), ser.getLastUpdate(), ser.getFrequency());
            pw.println(sum);

            allSeriesPw.printf("%s %-5s %-50s%n", sum, rel.getId(), rel.getName());

            String relsum = String.format("%s\t%s\t%d", rel.getId(), rel.getName(), serList.size());
            summary.add(relsum);

            totalProcessed++;
          }
        }
        else {
          pw.println("No data returned from FRED!");
          Debug.LOGGER.info("Warning. No data returned from FRED!");
          Utils.sleep(15000);
        }
      }
    }
    return totalProcessed;
  }

}
