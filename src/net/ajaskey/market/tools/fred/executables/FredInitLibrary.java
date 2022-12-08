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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.FredUtils;

/**
 * Class used to Initialize a new local FRED library.
 */
public class FredInitLibrary {

  private static List<FredInitLibrary> filList = new ArrayList<>();
  private static List<DataSeriesInfo>  dsiList = new ArrayList<>();

  private final static int shortPause = 5;
  private final static int midPause   = 10;
  private static int       longPause  = 15;

  private static List<String> invalidCodes = new ArrayList<>();

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
   */
  public static void main(String[] args) {

    Utils.makeDir("debug");
    Utils.makeDir("out");

    Debug.init("debug/FredInitLibrary.dbg", java.util.logging.Level.INFO);

    ApiKey.set();
    Utils.makeDir(FredUtils.getLibrary());
    FredUtils.setDataSeriesInfoFile("out/filteredSeriesSummary.txt");
    FredUtils.setLibrary("FredLib");

    FredInitLibrary.setBadCodes();

    FredInitLibrary.setFromCli(args);

    try {

      Debug.LOGGER.info(String.format("LibDir=%s  dataSeriesInfoFile=%s", FredUtils.getLibrary(), FredUtils.getDataSeriesInfoFile()));

      final List<String> codeNames = FredUtils.readSeriesList(FredUtils.getDataSeriesInfoFile());

      String codes = "Processing codes :" + Utils.NL;
      for (final String code : codeNames) {

        if (FredInitLibrary.isValidCode(code)) {
          final FredInitLibrary fil = new FredInitLibrary(code);
          FredInitLibrary.filList.add(fil);
          codes += code + Utils.NL;
        }
      }
      Debug.LOGGER.info(codes);

      final DateTime dt = new DateTime(2000, DateTime.JANUARY, 1);

      int moreToDo = 1;
      int lastMoreToDo = 0;
      while (moreToDo > 0) {

        moreToDo = FredInitLibrary.process(dt);

        Debug.LOGGER.info(String.format("%n--------------------------------%n%nReturn from Processing with moreToDo=%d.", moreToDo));
        if (moreToDo > 0) {
          // Case where all input codes are junk and will never be found at FRED.
          if (lastMoreToDo >= moreToDo) {

            Debug.LOGGER.info(String.format("Finished, only junk code(s) remain to be found.%n---------------------------------%n%n"));
            FredInitLibrary.writeJunkCodes();
            break;
          }
          lastMoreToDo = moreToDo;
          Debug.LOGGER.info(String.format("%nPausing %d seconds.%n---------------------------------%n%n", FredInitLibrary.midPause));
          Utils.sleep(FredInitLibrary.midPause * 1000);
        }
      }

      Debug.LOGGER.info(Utils.NL + "---------------------" + Utils.NL + "Processing Complete" + Utils.NL);

      for (final FredInitLibrary fil : FredInitLibrary.filList) {
        System.out.println(fil.getName());

        if (fil.dsi != null && fil.ds != null) {
          if (fil.dsi.isValid() && fil.ds.isValid()) {
            FredUtils.writeToLib(fil.dsi, fil.ds, FredUtils.getLibrary());
            FredInitLibrary.dsiList.add(fil.dsi);
          }
          else {
            Debug.LOGGER.info(String.format("Warning. Invalid FIL data %n%s", fil));
            if (fil.dsi != null) {
              Debug.LOGGER.info(String.format("Warning. Invalid FIL.DSI data %n%s", fil.dsi));
            }
            if (fil.ds != null) {
              Debug.LOGGER.info(String.format("Warning. Invalid FIL.DS data %n%s", fil.ds));
            }
          }
        }
        else {
          Debug.LOGGER.info(String.format("Warning. Invalid FIL data. Found nulls. %n%s", fil));
        }
      }

      Debug.LOGGER.info(Utils.NL + "---------------------" + Utils.NL + "Writing Files Complete" + Utils.NL);

      final String fname = "FredLib/new-data-series-info.txt";
      FredUtils.writeSeriesInfo(FredInitLibrary.dsiList, fname);

      Debug.LOGGER.info(Utils.NL + "---------------------" + Utils.NL + "Writing Data Series Info Complete" + Utils.NL);

    }
    catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * For filtering problem codes from input data.
   * 
   * @param code Series Id to check.
   * @return True if code is in preset list to filter.
   */
  private static boolean isValidCode(String code) {
    boolean ret = true;
    for (String s : invalidCodes) {
      if (s.equalsIgnoreCase(code.trim())) {
        ret = true;
        Debug.LOGGER.info(String.format("Invalid code found in list to process : %s", code));
        break;
      }
    }
    return ret;
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
  private static int process(DateTime dt) throws FileNotFoundException, IOException {

    int unprocessed = 0;
    int processed = 0;
    int errors = 0;
    boolean error = false;

    for (final FredInitLibrary fil : FredInitLibrary.filList) {

      if (!fil.isComplete()) {

        Debug.LOGGER.info(String.format("%n+++++++%nProcessing : %s", fil.getName()));
        if (fil.isDsiValid()) {
          Debug.LOGGER.info(String.format("Processing previously completed for DSI : %s", fil.getName()));
        }
        if (fil.isDsValid()) {
          Debug.LOGGER.info(String.format("Processing previously completed for DS : %s", fil.getName()));
        }

        /**
         * If no DSI returned from the query for data. If successful then query for DS
         * data.
         */
        if (fil.dsi == null) {
          Debug.LOGGER.info(String.format("Processing DSI : %s", fil.getName()));
          final DataSeriesInfo dsi = new DataSeriesInfo(fil.name, dt);
          if (dsi.isValid()) {
            Debug.LOGGER.info(String.format("DSI Set%n%s", dsi));
            fil.dsi = dsi;

            Debug.LOGGER.info(String.format("+++Processing DS : %s", fil.getName()));
            final DataSeries ds = new DataSeries(fil.dsi);
            if (ds.isValid()) {
              fil.ds = ds;
              processed++;
              Debug.LOGGER.info(String.format("DS Set : %s  processed=%d  unprocessed=%d%n%s%n", fil.getName(), processed, unprocessed, ds));
            }
            else {
              Debug.LOGGER.info(String.format("Failed DS query : %s   processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));
              unprocessed++;
              errors++;
              error = true;
            }
          }
          /**
           * DSI query failed.
           */
          else {
            Debug.LOGGER.info(String.format("Failed DSI query : %s   processed=%d  unprocessed=%d%n", fil.getName(), processed, unprocessed));
            unprocessed++;
            errors++;
            error = true;
          }

        }
        /**
         * DSI previously set but DS not set yet (failed previously).
         */
        else if (fil.ds == null) {
          final DataSeries ds = new DataSeries(fil.dsi);
          if (ds.isValid()) {
            fil.ds = ds;
            processed++;
            Debug.LOGGER.info(String.format("DS Set : %s  processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));
          }
          else {
            unprocessed++;
            errors++;
            error = true;
            Debug.LOGGER.info(String.format("Failed Processing DS : %s   processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));
          }
        }
      }

      if (error) {
        final int ptime = FredInitLibrary.shortPause * errors;
        Debug.LOGGER.info(String.format("%nQuery error=%d. Pausing for %d seconds.  unprocessed=%d%n", errors, ptime, unprocessed));
        Utils.sleep(FredInitLibrary.longPause * 1000);
        if (errors == 3) {
          errors = 0;
        }
        error = false;
      }
    }
    return unprocessed;
  }

  /**
   * Add codes here that cause problems during processing.
   */
  private static void setBadCodes() {
    invalidCodes.add("NAME");
  }

  private static void setFromCli(String[] args) {
    /**
     * CLI
     */
    final Options options = new Options();
    final Option debugfile = Option.builder().longOpt("debug").argName("dbg").hasArg().desc("debug file override").build();
    final Option datalib = Option.builder().longOpt("datalib").argName("lib").hasArg().desc("data library override").build();
    options.addOption(debugfile);
    options.addOption(datalib);

    final CommandLineParser parser = new DefaultParser();
    try {
      final CommandLine line = parser.parse(options, args);
      if (line.hasOption("debug")) {
        System.out.println(line.getOptionValue(debugfile));
      }
      if (line.hasOption("datalib")) {
        System.out.println(line.getOptionValue(datalib));
      }
    }
    catch (final ParseException e1) {
      e1.printStackTrace();
    }

  }

  /**
   * Write a list of codes not processed. Possible bad code names.
   */
  private static void writeJunkCodes() {

    try (PrintWriter pw = new PrintWriter("out/junk-codes-found.txt")) {
      for (final FredInitLibrary fil : FredInitLibrary.filList) {
        System.out.println(fil.getName());

        if (fil.dsi == null || fil.ds == null) {
          pw.println(fil.getName());
        }
      }
    }
    catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private final String  name;
  public DataSeriesInfo dsi;
  public DataSeries     ds;

  /**
   * Constructor
   *
   * @param n Name of FRED ID Code
   */
  private FredInitLibrary(String n) {
    this.name = n;
    this.dsi = null;
    this.ds = null;
  }

  public boolean isComplete() {
    boolean ret = false;
    if (this.dsi != null && this.ds != null) {
      if (this.dsi.isValid()) {
        if (this.ds.isValid()) {
          ret = true;
        }
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    String ret = String.format("Series     : %s%n", this.name);
    if (this.dsi != null) {
      ret += String.format(" DSI Valid : %s%n", this.dsi.isValid());
    }
    else {
      ret += String.format(" DSI is null!%n");
    }
    if (this.ds != null) {
      ret += String.format(" DS Valid  : %s%n", this.ds.isValid());
    }
    else {
      ret += String.format(" DS is null!%n");
    }
    ret += String.format(" Complete  : %s", this.isComplete());
    return ret;
  }

  private DataSeries getDs() {
    return this.ds;
  }

  private DataSeriesInfo getDsi() {
    return this.dsi;
  }

  private String getName() {
    return this.name;
  }

  private boolean isDsiValid() {
    boolean ret = false;
    if (this.getDsi() != null) {
      ret = this.dsi.isValid();
    }
    return ret;
  }

  private boolean isDsValid() {
    boolean ret = false;
    if (this.getDs() != null) {
      ret = this.ds.isValid();
    }
    return ret;
  }

}
