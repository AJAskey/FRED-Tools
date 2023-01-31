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
import java.util.Collections;
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
import net.ajaskey.market.tools.fred.FredUtils;
import net.ajaskey.market.tools.fred.LocalFormat;
import net.ajaskey.market.tools.fred.queries.Observations;
import net.ajaskey.market.tools.fred.queries.Series;

/**
 * Class used to maintain data in a local FRED library.
 */
public class FredUpdateLocal {

  public enum FredMode {
    FULL, UPDATE, OVERRIDE;
  }

  private static FredMode mode = FredMode.UPDATE;
  // private static String fredlib = "D:/data2/MA/CSV Data/FRED-Download";
  private static String fredlib  = "fredlib";
  private static String inputIds = "input/filteredSeriesSummary.txt";
  private static int    downloadKnt;

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
   * @param args Command line arguements. See <b>setFromCli()</b>.
   */
  public static void main(String[] args) {

    /**
     * Key command line inputs:
     *
     * mode - FULL or UPDATE (Default : UPDATE)
     *
     * fredlib - Directory with current and for new data files. (Default :
     * "fredlib")
     *
     * inputIds - File with LocalFormat data listing Series IDs to be processed.
     * (Default : "out/filteredSeriesSummary.txt")
     *
     */

    Utils.makeDir("debug");
    Utils.makeDir("out");
    Utils.makeDir(FredUpdateLocal.fredlib);

    downloadKnt = 0;

    FredUpdateLocal.setFromCli(args);

    Debug.init("debug/FredUpdateLocal.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    String dbg = String.format("Mode=%s    fredlib=%s    inputIds=%s", FredUpdateLocal.mode, FredUpdateLocal.fredlib, FredUpdateLocal.inputIds);
    Debug.LOGGER.info(dbg);

    final List<LocalFormat> lfList = LocalFormat.readSeriesList(FredUpdateLocal.inputIds, FredUpdateLocal.fredlib);
    Collections.sort(lfList, LocalFormat.sorterAbc);

    final List<LocalFormat> overridesList = LocalFormat.readRawList("input/overrides.txt", lfList);

    String ids = "Processing Ids :" + Utils.NL;
    for (final LocalFormat lf : overridesList) {
      ids += lf.getId() + "    (Override)" + Utils.NL;
    }
    for (final LocalFormat lf : lfList) {
      ids += lf.getId() + Utils.NL;
    }
    Debug.LOGGER.info(ids);

    for (final LocalFormat lf : overridesList) {
      System.out.println(lf.getId());
      Debug.LOGGER.info(String.format("%n%n------%n"));
      FredUpdateLocal.process(lf, FredUpdateLocal.fredlib, 5, 8);
    }

    /**
     * Only process OVERRIDEs
     */
    if (FredUpdateLocal.mode == FredMode.OVERRIDE) {
      Debug.LOGGER.info(String.format("%n%n------%nProcessing completed. Downloaded = %d", downloadKnt));
      return;
    }

    dbg = "";

    for (final LocalFormat lf : lfList) {
      boolean success = false;
      if (FredUpdateLocal.mode == FredMode.FULL) {
        success = FredUpdateLocal.process(lf, FredUpdateLocal.fredlib, 5, 8);
      }
      else {
        success = FredUpdateLocal.update(lf, FredUpdateLocal.fredlib, 5, 8);
      }
      if (success) {
        lf.setLocalFileLastObs();
        dbg += lf.getId() + Utils.NL;
        String s = String.format("%-25s %s", lf.getId(), lf.getFrequency());
        System.out.println(s);
      }
    }

    Debug.LOGGER.info(Utils.NL + dbg);

    Collections.sort(lfList, LocalFormat.sorter);
    try (PrintWriter pw = new PrintWriter("out/Fred-Status.txt")) {
      for (LocalFormat lf : lfList) {
        pw.println(lf.formatlineRel());
      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    Debug.LOGGER.info(String.format("%n%n------%nProcessing completed. Downloaded = %d", downloadKnt));

    System.out.println("Done.");
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
  static boolean process(LocalFormat lf, String fredlib, int retries, int delay) {

    boolean success = false;

    Debug.LOGGER.info(String.format("Processing... %n%s", lf));

    final Observations obs = Observations.queryObservation(lf.getId(), retries, delay);

    if (obs != null) {

      if (obs.isValid()) {

        Debug.LOGGER.info(String.format("Returned observations for %s%n%s", lf.getId(), obs));

        FredUtils.writeToLib(obs, lf, fredlib);
        success = true;
        downloadKnt++;
      }
      else {
        Debug.LOGGER.info(String.format("Observations for %s is not valid.", lf.getId()));
      }
    }
    else {
      Debug.LOGGER.info(String.format("Observations for %s is null.", lf.getId()));
    }

    return success;
  }

  /**
   *
   * @param lf
   * @param fredlib
   */
  static boolean update(LocalFormat lf, String fredlib, int retries, int delay) {

    boolean success = false;

    Debug.LOGGER.info(Utils.NL + Utils.NL);
    Debug.LOGGER.info(String.format("LF :%n%s", lf));
    boolean doProcess = false;

//    String s = String.format("%-25s %s", lf.getId(), lf.getFrequency());
//    System.out.println(s);

    if (lf.getLocalFileLastObs() == null) {
      doProcess = true;
      Debug.LOGGER.info(String.format("doProcess - no local file"));
    }
    else if (lf.getLocalFileLastObs().isLessThan(lf.getLastObservation())) {
      doProcess = true;
      Debug.LOGGER.info(String.format("doProcess - Last Local File Observation: %s less than Last Observation: %s", lf.getLocalFileLastObs(),
          lf.getLastObservation()));
    }
    else if (lf.getFrequency().toLowerCase().contains("daily")) {
      doProcess = true;
      Debug.LOGGER.info(String.format("doProcess - daily"));
    }
    else {
      Debug.LOGGER.info(String.format("calling checkDates"));
      doProcess = checkDates(lf);
    }

    Debug.LOGGER.info(String.format("Updating data : %s", doProcess));
    if (doProcess) {
      success = FredUpdateLocal.process(lf, fredlib, retries, delay);
    }

    return success;
  }

  /**
   * 
   * @param lf
   * @return
   */
  private static boolean checkDates(LocalFormat lf) {
    boolean doProcess = false;

    DateTime nextUpdate = new DateTime(lf.getLocalFile().lastModified());
    DateTime today = new DateTime();

    if (lf.getFrequency().toLowerCase().contains("biweek")) {
      nextUpdate.add(DateTime.DATE, 14);
    }
    else if (lf.getFrequency().toLowerCase().contains("week")) {
      nextUpdate.add(DateTime.DATE, 7);
    }
    else if (lf.getFrequency().toLowerCase().contains("month")) {
      nextUpdate.add(DateTime.MONTH, 1);
    }
    else if (lf.getFrequency().toLowerCase().contains("quarter")) {
      nextUpdate.add(DateTime.MONTH, 3);
    }
    else {
      nextUpdate = null;
    }

    if (nextUpdate != null) {

      boolean dateCheck = today.isGreaterThan(nextUpdate);
      Debug.LOGGER.info(String.format("today=%s greater than nextUpdate=%s == %s", today, nextUpdate, dateCheck));

      doProcess = dateCheck;
    }

    return doProcess;
  }

  /**
   * 
   * @param lf
   * @return
   */
  private static boolean checkDates2(LocalFormat lf) {
    boolean doProcess = false;

    DateTime nextUpdate = new DateTime(lf.getLastUpdate());
    DateTime today = new DateTime();

    if (lf.getFrequency().toLowerCase().contains("biweek")) {
      nextUpdate.add(DateTime.DATE, 14);
    }
    else if (lf.getFrequency().toLowerCase().contains("week")) {
      nextUpdate.add(DateTime.DATE, 7);
    }
    else if (lf.getFrequency().toLowerCase().contains("month")) {
      nextUpdate.add(DateTime.DATE, 30);
    }
    else if (lf.getFrequency().toLowerCase().contains("quarter")) {
      nextUpdate.add(DateTime.DATE, 90);
    }
    else {
      nextUpdate = null;
    }

    if (nextUpdate != null) {
      Debug.LOGGER.info(String.format("nextUpdate=%s  today=%s", nextUpdate, today));
      if (today.isGreaterThanOrEqual(nextUpdate)) {
        doProcess = true;
        Debug.LOGGER.info(String.format("%s doProcess check1 is true.", lf.getId()));
      }
    }

    if (doProcess) {
      doProcess = queryFred(lf);
      if (doProcess) {
        Debug.LOGGER.info(String.format("%s doProcess check2 is true.", lf.getId()));
      }
    }

    return doProcess;
  }

  /**
   * 
   * @param lf
   * @return
   */
  private static boolean queryFred(LocalFormat lf) {

    boolean ret = false;

    if (lf.getLocalFile() != null) {

      Debug.LOGGER.info(String.format("LF -->%n%s", lf));

      DateTime lo = FredUtils.getLastObservation(lf.getLocalFile());

      Series series = Series.query(lf.getId(), 8, 8);

      if (series != null) {

        Debug.LOGGER.info(String.format("series -->%n%s", series));

        if (series.getLastUpdate().isGreaterThan(lo)) {
          Debug.LOGGER.info(String.format("Ret is TRUE"));
          ret = true;
        }
      }
      else {
        Debug.LOGGER.info(String.format("Warning ... Series is NULL"));
      }
    }

    return ret;
  }

  /**
   *
   * @param args
   */
  private static void setFromCli(String[] args) {
    /**
     * CLI
     */
    final Options options = new Options();
    final Option debugfile = Option.builder().longOpt("debug").argName("d").hasArg().desc("debug file override").build();
    final Option fredlib = Option.builder().longOpt("fredlib").argName("l").hasArg().desc("FRED Library Directory").build();
    final Option mode = Option.builder().longOpt("mode").argName("m").hasArg().desc("Mode of Operation").build();
    final Option input = Option.builder().longOpt("input").argName("i").hasArg().desc("File of Ids").build();
    options.addOption(debugfile);
    options.addOption(fredlib);
    options.addOption(mode);
    options.addOption(input);

    final CommandLineParser parser = new DefaultParser();
    try {
      final CommandLine line = parser.parse(options, args);
      if (line.hasOption("debug")) {
        System.out.println(line.getOptionValue(debugfile));
      }
      if (line.hasOption("fredlib")) {
        System.out.println(line.getOptionValue(fredlib));
        FredUpdateLocal.setLib(line.getOptionValue(fredlib));
      }
      if (line.hasOption("mode")) {
        System.out.println(line.getOptionValue(mode));
        FredUpdateLocal.setMode(line.getOptionValue(mode));
      }
      if (line.hasOption("input")) {
        System.out.println(line.getOptionValue(input));
        FredUpdateLocal.setInput(line.getOptionValue(input));
      }
    }
    catch (final ParseException e1) {
      e1.printStackTrace();
    }

  }

  private static void setInput(String infile) {
    FredUpdateLocal.inputIds = infile;

  }

  private static void setLib(String libdir) {
    FredUpdateLocal.fredlib = libdir;
  }

  private static void setMode(String m) {

    try {
      FredUpdateLocal.mode = FredMode.UPDATE;
      if (m.toUpperCase().equals("FULL")) {
        FredUpdateLocal.mode = FredMode.FULL;
      }
      else if (m.toUpperCase().equals("OVERRIDE")) {
        FredUpdateLocal.mode = FredMode.OVERRIDE;
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
      FredUpdateLocal.mode = FredMode.UPDATE;
    }

  }

}
