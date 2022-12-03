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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeries.AggregationMethodType;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.DataValues;

public class FredDataDownloader {

  public static final int consecutiveRetryFailures = 9;

  public static final int longSleep = 35000;

  public static final int maxRetries = 1;

  public static int retryCount = 0;

  // public static final Logger LOGGER =
  // Logger.getLogger(FredDataDownloader.class.getName());

  public static PrintWriter tryAgainFile = null;

  /**
   * net.ajaskey.market.tools.fred.main
   *
   * @param args
   * @throws IOException
   */
  public static void main(final String[] args) throws IOException {

    Debug.init("debug/FredDataDownloader.log", java.util.logging.Level.INFO);

    FredDataDownloader.tryAgainFile = new PrintWriter("out/fred-try-again.txt");

    Utils.makeDir(FredCommon.fredPath);

    // final File folder = new File(FredCommon.fredPath);

    List<String> codeNames = new ArrayList<>();

    codeNames = FredCommon.readSeriesList("data/fred-series-new-names.txt");
    Collections.sort(codeNames);
    String codes = "Processing codes :" + Utils.NL;
    for (final String s : codeNames) {
      codes += s + Utils.NL;
    }
    Debug.Log(codes);

    Utils.sleep(1000);

    final List<DataSeriesInfo> allNames = FredCommon.queryFredDsi(codeNames);

    Debug.Log("\n----------------------------------------------------------\n");

    for (final DataSeriesInfo dsi : allNames) {
      FredDataDownloader.process(dsi);
    }

    FredDataDownloader.tryAgainFile.close();

    System.out.println("Done.");
  }

  /**
   *
   * net.ajaskey.market.tools.fred.process
   *
   * @param seriesDsi
   */
  private static void process(final DataSeriesInfo seriesDsi) {

    final DataSeries ds = new DataSeries(seriesDsi.getName().trim());

    ds.setAggType(AggregationMethodType.EOP);
    ds.setRespType(DataSeries.ResponseType.LIN);

    Debug.Log("Querying for data values ...\n");

    List<DataValues> dvList = null;
    for (int i = 0; i <= FredDataDownloader.maxRetries; i++) {
      Utils.sleep(1000 * 5 * i + 250);
      dvList = ds.getValues(0.0, false, false);
      if (dvList.size() > 0) {
        System.out.println(seriesDsi.getName());
        FredDataDownloader.retryCount = 0;
        break;
      }
      FredDataDownloader.retryCount++;
      if (i < FredDataDownloader.maxRetries) {
        Debug.Log(String.format("\tQuery dvList retry for %s ...%n", seriesDsi.getName()));
      }
    }

    if (dvList != null && dvList.size() > 0) {

      Debug.Log(String.format("Writing to Optuma%n%s%nvalues : %d%n", ds, dvList.size()));

      final String outname = FredCommon.toFullFileName(seriesDsi.getName(), seriesDsi.getTitle());

      FredCommon.writeToOptuma(dvList, outname, seriesDsi.getName(), seriesDsi.getUnits(), seriesDsi.getFrequency(), false);

      // Debug.pwDbg.println(ds);
    }
    else {
      Debug.Log(String.format("%nZero Data Values: %s : %s%n", seriesDsi.getName(), seriesDsi.getTitle()));

      FredDataDownloader.tryAgainFile.println(seriesDsi.getName());
      FredDataDownloader.tryAgainFile.flush();
      if (FredDataDownloader.retryCount > FredDataDownloader.consecutiveRetryFailures) {
        Debug.Log(String.format("Too many retries (%d). Sleeping %d seconds.%n", FredDataDownloader.retryCount, FredDataDownloader.longSleep / 1000));

        Utils.sleep(FredDataDownloader.longSleep);
        FredDataDownloader.retryCount = 0;
      }
    }

    // for (DataValues dv : dvList) {
    // System.out.println(dv);

    // final String title = FredCommon.cleanTitle(ds.getInfo().getTitle());
    // System.out.println(ds.getName() + "\t" + ds.getName() + "\t" + title);
    // }
  }

}
