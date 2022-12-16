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
import java.util.ArrayList;
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
public class FredInitLibrary {

  private static List<String> invalidIds = new ArrayList<>();

  /**
   * Main procedure:
   *
   * <p>
   * 1. Reads data file with list of codes from FRED and creates a list of
   * FredInitLibrary class of potential data.
   * </p>
   *
   * <p>
   * 2. Queries FRED for DataSeriesInfo and date/value list for each code.
   * Continue to try until no progress is made (meaning possible bad code names
   * from input).
   * </p>
   *
   * <p>
   * 3. The data for codes retrieved from FRED is written into file pairs per
   * code. One file has the code as the file name. The other file has a longer
   * description of what is in the file within '[]'.
   * </p>
   *
   * <p>
   * 4. A CSV file of those codes processed is created for review after the
   * program end.
   * </p>
   *
   * @param args No used.
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {

    Utils.makeDir("debug");
    Utils.makeDir("out");

    Debug.init("debug/FredInitLibrary.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    List<LocalFormat> lfList = LocalFormat.readSeriesList("out/filteredSeriesSummary.txt", "FredLib");

    String ids = "Processing Ids :" + Utils.NL;
    for (LocalFormat lf : lfList) {
      ids += lf.getId() + Utils.NL;
    }
    Debug.LOGGER.info(ids);

    for (LocalFormat lf : lfList) {
      process(lf, "FredLib");
    }

    Debug.LOGGER.info(Utils.NL + "Processing completed");
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
  private static void process(LocalFormat lf, String fredlib) {

    Debug.LOGGER.info(String.format("Processing... %n%s", lf));

    Observations obs = Observations.queryObservation(lf.getId(), 8, 8);

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

}
