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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.FredUtils;
import net.ajaskey.market.tools.fred.LocalFormat;
import net.ajaskey.market.tools.fred.queries.Observations;

/**
 * Class used to Initialize a new local FRED library.
 */
public class FredUpdateLocal {

  public enum FredMode {
    FULL, UPDATE;
  }

  /**
   * Main procedure:
   *
   * <p>
   * 1. Reads data file with list of Series Ids in LocalFormat that creates a list
   * of of potential data.
   * </p>
   *
   * <p>
   * If mode is FULL then all Series Ids are processed. If mode is UPDATE then
   * those Series Id with new data at FRED (vs local data file) are processed.
   * </p>
   *
   * <p>
   * 3. The data for Ids is retrieved from FRED and written into file pairs. One
   * file has the Id as the file name. The other file has a longer description of
   * what is in the file within '[]'.
   * </p>
   *
   *
   * @param args None used.
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {

    /**
     * Key internal variables:
     *
     * mode - FULL or UPDATE
     *
     * fredlib - Directory with current and for new data files.
     *
     * inputIds - File with LocalFormat data listing Ids to be processed.
     *
     */
    final FredMode mode = FredMode.UPDATE;
    final String fredlib = "D:\\data2\\MA\\CSV Data\\FRED-Download";
    final String inputIds = "out/filteredSeriesSummary.txt";

    Utils.makeDir("debug");
    Utils.makeDir("out");

    Debug.init("debug/FredUpdateLocal.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    Debug.LOGGER.info(String.format("Mode=%s    fredlib=%s", mode, fredlib));

    final List<LocalFormat> lfList = LocalFormat.readSeriesList(inputIds, "FredLib");

    String ids = "Processing Ids :" + Utils.NL;
    for (final LocalFormat lf : lfList) {
      ids += lf.getId() + Utils.NL;
    }
    Debug.LOGGER.info(ids);

    for (final LocalFormat lf : lfList) {
      if (mode == FredMode.FULL) {
        FredUpdateLocal.process(lf, fredlib, 5, 8);
      }
      else {
        FredUpdateLocal.update(lf, fredlib, 5, 8);
      }
    }

    Debug.LOGGER.info(String.format("%n%n------%nProcessing completed"));
  }

  /**
   * Process unprocessed data from filList. The number of entries processed is
   * returned. Sleeps are used to allow the FRED server to catch up.
   *
   * @param dt
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  static void process(LocalFormat lf, String fredlib, int retries, int delay) {

    Debug.LOGGER.info(String.format("Processing... %n%s", lf));

    final Observations obs = Observations.queryObservation(lf.getId(), retries, delay);

    if (obs != null) {

      if (obs.isValid()) {

        Debug.LOGGER.info(String.format("Returned observations for %s%n%s", lf.getId(), obs));

        FredUtils.writeToLibNew(obs, lf, fredlib);
      }
      else {
        Debug.LOGGER.info(String.format("Observations for %s is not valid.", lf.getId()));
      }
    }
    else {
      Debug.LOGGER.info(String.format("Observations for %s is null.", lf.getId()));
    }
  }

  /**
   *
   * @param lf
   * @param fredlib
   */
  private static void update(LocalFormat lf, String fredlib, int retries, int delay) {

    Debug.LOGGER.info(String.format("%n---%nChecking %s", lf.getId()));
    boolean doProcess = false;

    if (lf.getFrequency().toLowerCase().contains("daily")) {
      doProcess = true;
      Debug.LOGGER.info(String.format("doProcess - daily"));
    }
    else if (lf.getLocalFileDate() == null) {
      doProcess = true;
      Debug.LOGGER.info(String.format("doProcess - no local file"));
    }
    else {
      if (lf.getLocalFileDate().isLessThan(lf.getLastUpdate())) {
        doProcess = true;
        Debug.LOGGER.info(String.format("doProcess - local file out of date"));
      }
    }

    if (doProcess) {
      FredUpdateLocal.process(lf, fredlib, retries, delay);
    }
  }

}
