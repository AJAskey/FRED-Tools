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

package net.ajaskey.market.tools.fred.legacy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.DsiSorter;

public class FredBookkeeping {

  private static List<DataSeriesInfo> dsiList     = new ArrayList<>();
  private static final String         fsiFilename = "data/fred-series-info.txt";

  // private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd
  // HH:mm:ss.SSS");

  private static final String tryAgainFilename = "out/fred-try-again.txt";

  /**
   * net.ajaskey.market.tools.fred.main
   *
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {

    Utils.makeDir("debug");

    Debug.init("debug/fred-bookkeeping.dbg");
    FredDataDownloader.tryAgainFile = new PrintWriter(FredBookkeeping.tryAgainFilename);

    final String folder = FredCommon.fredPath;

    final Set<String> uniqCodes = new HashSet<>();

    final String[] ext = { "csv" };
    final List<File> files = Utils.getDirTree(folder, ext);
    for (final File file : files) {
      final String name = file.getName();
      // Ignore derived TREAST files
      if (FredBookkeeping.validFileName(name)) {
        final String f1 = name.replace(".csv", "");
        final String f2 = f1.replace("[", "").trim();
        final int idx = f2.indexOf("]");
        String f3 = f2;
        if (idx > 0) {
          f3 = f2.substring(0, idx).trim();
        }
        uniqCodes.add(f3);
      }
    }

    // for later : List<DataSeriesInfo> oldList =
    // FredCommon.readSeriesInfo(fsiFilename );

    final List<String> codes = new ArrayList<>(uniqCodes);
    Collections.sort(codes);

    FredBookkeeping.process(codes, "out/CodesToUpdate.txt");

    FredDataDownloader.tryAgainFile.close();

    Utils.sleep(2500);
    Debug.LOGGER.info("Processing retry attempts...");

    final List<String> retry = FredCommon.readSeriesList(FredBookkeeping.tryAgainFilename);
    FredBookkeeping.process(retry, "out/RetryCodesToAdd.txt");

    FredDataDownloader.tryAgainFile = new PrintWriter(FredBookkeeping.tryAgainFilename);

    System.out.println(codes.size());

    Collections.sort(FredBookkeeping.dsiList, new DsiSorter());
    FredCommon.writeSeriesInfo(FredBookkeeping.dsiList, FredBookkeeping.fsiFilename);

    FredDataDownloader.tryAgainFile.close();

  }

  /**
   *
   * net.ajaskey.market.tools.fred.process
   *
   * @param codes
   * @throws FileNotFoundException
   */
  private static void process(final List<String> codes, String updateFileName) throws FileNotFoundException {

    try (PrintWriter pwUpdate = new PrintWriter(updateFileName)) {

      // int knt = 0;
      for (final String code : codes) {
        final File f = new File(FredCommon.fredPath + "/" + code + ".csv");
        final DateTime fileLastUpdate = new DateTime(f.lastModified());

        // Debug.log(String.format("Processing existing file %s last updated on %s",
        // code, fileLastUpdate));

        final DataSeriesInfo dsi = FredCommon.queryFredDsi(code, fileLastUpdate);
        if (dsi != null) {

          // System.out.printf("%-20s --> %-20s\t\t%15s%n", code,
          // dsi.getFileDt().toFullString(), dsi.getLastUpdate().toFullString());
          FredBookkeeping.dsiList.add(dsi);

          Debug.LOGGER.info(dsi.toString());

          final boolean needsUpdate = dsi.getLastUpdate().isGreaterThan(dsi.getFileDt());
          if (needsUpdate) {
            pwUpdate.printf("%s\t%s%n", dsi.getName(), dsi.getTitle());
            System.out.printf("%s\t%s%n", dsi.getName(), dsi.getTitle());
          }
        }
        // Set to lower number for testing
        // if (++knt > 20050) {
        // break;
        // }
      }
    }
  }

  /**
   * net.ajaskey.market.tools.fred.validFileName
   *
   * @param name
   * @return
   */
  private static boolean validFileName(String name) {

    boolean ret = true;

    if (name.contains("TREAST-")) {
      ret = false;
    }
    else if (name.contains("Export minus Import")) {
      ret = false;
    }
    else if (name.contains("SPX Growth vs ")) {
      ret = false;
    }
    else if (name.contains("Value Inventory to Shipments for ")) {
      ret = false;
    }

    return ret;
  }

}
