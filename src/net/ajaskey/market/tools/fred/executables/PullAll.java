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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.LocalFormat;
import net.ajaskey.market.tools.fred.queries.Release;
import net.ajaskey.market.tools.fred.queries.Series;

/**
 *
 * Class is designed to query FRED for a list of all Releases. Then it queries
 * FRED about every Series in each Release.
 *
 * Files are output for use in subsequent processing tools. The
 * <b>lastUpdate</b> field is very useful.
 *
 */
public class PullAll {

  static PrintWriter allSeriesPw = null;

  /**
   *
   * Queries all releases and series description data and writes it to organized
   * files.
   *
   * @param args None expected.
   */
  public static void main(String[] args) {

    Debug.init("debug/PullAll.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    try {
      // Check the debug log to see if retries and/or delays needs to be modified. I
      // have found that 7 and 15 works well but it is a tradeoff between processing
      // time and internal retry loops.
      PullAll.processAll("FredSeries", "FredLib", 6, 7);
    }
    catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Main is used as wrapper here so processAll can be call from and external
   * class.
   *
   * @param relList   A list of Release names to process
   * @param seriesLib Directory where Series data is to be stored.
   * @param fredlib   Directory where Date/Value pairs are stored.
   * @param retries   FRED server times out a lot (depending on use). The URL is
   *                  sent to FRED 'retries' times.
   * @param delay     Time to delay between URL retries in seconds.
   *
   * @throws FileNotFoundException File problems exception.
   */
  public static void processAll(String serieslib, String fredlib, int retries, int delay) throws FileNotFoundException {

    PullAll.allSeriesPw = new PrintWriter("out/allSeriesSummary.txt");

    final List<Release> relList = Release.queryReleases();

    final int num = PullAll.process(relList, serieslib, fredlib, retries, delay);

    System.out.printf("processed = %d%n", num);

    PullAll.allSeriesPw.close();

    Debug.LOGGER.info(Utils.NL + "-----------------------" + Utils.NL + "Processing complete!");
  }

  /**
   *
   * @param relList   A list of Release names to process
   * @param seriesLib Directory where Series data is to be stored.
   * @param fredlib   Directory where Date/Value pairs are stored.
   * @param retries   FRED server times out a lot (depending on use). The URL is
   *                  sent to FRED 'retries' times.
   * @param delay     Time to delay between URL retries in seconds.
   *
   * @return Number of items processed
   *
   * @throws FileNotFoundException File problems exception.
   */
  private static int process(List<Release> relList, String seriesLib, String fredlib, int retries, int delay) throws FileNotFoundException {

    int totalProcessed = 0;

    for (final Release rel : relList) {

      final String cleanname = rel.getName().replaceAll("/", "").replaceAll(":", "").replaceAll("\\.", "_").replaceAll("\"", "").replaceAll("&", "_");
      final String tmp = String.format("%s/Id_%s%s.txt", seriesLib, rel.getId(), cleanname);
      final String fn = tmp.replaceAll(" ", "");

      // Remove existing file so new file will show date of creation. Must be a
      // Windows feature.
      final File f = new File(fn);
      if (f.exists()) {
        f.delete();
      }

      try (PrintWriter pw = new PrintWriter(fn)) {

        final String s = String.format("Release  Id : %s\t%s", rel.getId(), rel.getName());
        pw.println(s);

        final List<Series> serList = Series.querySeriesPerRelease(rel.getId(), retries, delay);

        System.out.println(String.format("Processed querySeriesPerRelease for %-25s %5d  %-120s %s", rel.getId(), serList.size(), rel.getName(), fn));

        if (serList.size() > 0) {
          for (final Series ser : serList) {

            final LocalFormat lf = new LocalFormat(ser, rel.getId(), rel.getName(), fredlib);
            final String sum = lf.formatline();
            pw.println(sum);

            PullAll.allSeriesPw.printf("%s  %-3s %-1s%n", sum, rel.getId(), rel.getName());

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
