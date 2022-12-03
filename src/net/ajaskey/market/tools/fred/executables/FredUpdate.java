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
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.FredUtils;

/**
 * Class used to update data within a local FRED library.
 */
public class FredUpdate {

  final static String             ftLibDir   = FredUtils.fredPath + "/data";
  final static String             ftDataDir  = FredUtils.fredPath + "/data";
  private static List<FredUpdate> updateList = new ArrayList<>();

  /**
   * Main procedure:
   *
   * <p>
   * 1. Reads data file with list of codes from FRED and compares to the date of
   * file currently within the local library.
   * </p>
   *
   * <p>
   * 2. Queries FRED for DataSeriesInfo and date/value list for each code
   * requiring an update.
   * </p>
   *
   * <p>
   * 3. The data for codes retrieved from FRED is written into file pairs per
   * code. One file has the code as the file name. The other file has a longer
   * description of what is in the file within '[]'.
   * </p>
   *
   * @param args Not used.
   */
  public static void main(String[] args) {

    try {

      ApiKey.set();

      Utils.makeDir(FredUpdate.ftDataDir);
      Utils.makeDir("debug");
      Utils.makeDir("out");

      Debug.init("debug/FredUpdate.dbg", java.util.logging.Level.INFO);

      final List<String> codeNames = FredUtils.readSeriesList(FredUpdate.ftDataDir + "/fred-series-info.txt");
      final List<FredUpdate> fuList = new ArrayList<>();

      final DateTime dtlong = new DateTime(2000, DateTime.JANUARY, 1);
      for (final String code : codeNames) {
        final FredUpdate fu = new FredUpdate(code, dtlong);
        fuList.add(fu);
      }

      int moreToDo = 1;
      int lastMoreToDo = 0;
      while (moreToDo > 0) {
        moreToDo = FredUpdate.process(fuList);
        Debug.Log(String.format("%n%n-----%nEnd Processing. moreToDo=%d", moreToDo));
        if (moreToDo > 0) {
          if (moreToDo == lastMoreToDo) {
            break;
          }
          lastMoreToDo = moreToDo;
          Utils.sleep(10000);
          Debug.Log(String.format("%n-----%nBeginning Nex Processing Iteration. moreToDo=%d", moreToDo));
        }
      }

      Utils.sleep(5000);
      Debug.Log(
          String.format("%n%n---------------------------------%n%nProcessing Values and Writing Output  updateList.size=%d.", updateList.size()));

      Debug.Log(String.format("Codes requiring an update: %d%n", FredUpdate.updateList.size()));
      String dbg = "";
      for (final FredUpdate fu : FredUpdate.updateList) {
        dbg += fu.getName() + Utils.NL;
      }
      Debug.Log(dbg);

      moreToDo = 1;
      lastMoreToDo = 0;
      while (moreToDo > 0) {
        moreToDo = FredUpdate.processValues();
        Debug.Log(String.format("%n-----%n%nEnd Processing Values. moreToDo=%d", moreToDo));
        if (moreToDo > 0) {
          if (moreToDo == lastMoreToDo) {
            break;
          }
          lastMoreToDo = moreToDo;
          Utils.sleep(10000);
        }
      }

      Debug.Log(String.format("%n%n------------------------------------------------%n%n"));

      for (final FredUpdate fu : updateList) {
        if (fu.isUpdate()) {
          Debug.Log(String.format("%n---%nFredUtils : %s", fu));
        }
      }

      Debug.Log(String.format("%n%n---------------Processing Complete ---------------------------------"));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Process unprocessed DataSeriesInfo data from list. The number of entries
   * processed is returned.
   * 
   * Sleeps are used to allow the FRED server to catch up.
   * 
   * @param fuList
   * @return Number of unprocessed entries in list.
   */
  private static int process(List<FredUpdate> fuList) {

    int unprocessed = 0;

    for (final FredUpdate fu : fuList) {

      if (fu.dsi == null) {
        Debug.Log(String.format("Processing code=%s", fu.getName()));

        final File f = new File(FredUpdate.ftDataDir + "/" + fu.getName() + ".csv");
        DateTime dt;
        if (f.exists()) {
          dt = new DateTime(f.lastModified());
          dt.add(DateTime.DATE, -3); // temp for testing
          Debug.Log(String.format("Code=%s file found with date=%s", fu.getName(), dt));
        }
        else {
          dt = new DateTime(2000, DateTime.JANUARY, 1);
        }
        final DataSeriesInfo dsi = new DataSeriesInfo(fu.getName(), dt);
        if (dsi.isValid()) {
          Debug.Log(String.format("code=%s  DSI found.%n%s", fu.getName(), dsi));
          fu.dsi = dsi;
          if (dsi.getLastUpdate().isGreaterThan(dt)) {
            fu.update = true;
            final FredUpdate aCopy = new FredUpdate(fu);
            FredUpdate.updateList.add(aCopy);
            Debug.Log(String.format("Data required update.   LastUpdate=%s > dt=%s%n%s---%n", dsi.getLastUpdate(), dt, aCopy));
          }
          else {
            fu.update = false;
            Debug.Log(String.format("No update required.   LastUpdate=%s > dt=%s%n---%n", dsi.getLastUpdate(), dt));
          }
        }
        else {
          Debug.Log(String.format("code=%s  DSI not found.", fu.getName()));
          unprocessed++;
          Utils.sleep(10000);
        }
      }
    }
    return unprocessed;
  }

  /**
   * Process unprocessed DataSeries and DataValue data from list. The number of
   * entries processed is returned.
   * 
   * Successfully return date/value pair data is written to FRED library.
   * 
   * Sleeps are used to allow the FRED server to catch up.
   * 
   * @return Number of unprocessed entries in list.
   */
  private static int processValues() {

    int unprocessed = 0;

    for (final FredUpdate fu : FredUpdate.updateList) {

      if (fu.isUpdate()) {

        Debug.Log(String.format("Processing code=%s", fu.getName()));

        final DataSeries ds = new DataSeries(fu.dsi);
        if (ds.isValid()) {
          fu.ds = ds;
          fu.update = false;
          FredUtils.writeToLib(fu.dsi, fu.ds, FredUpdate.ftLibDir);
          Debug.Log(String.format("DS Set : %s  unprocessed=%d%n%s", fu.getName(), unprocessed, ds));
        }
        else {
          unprocessed++;
          Debug.Log(String.format("code=%s  DS not found.  uprocesssed=%d", fu.getName(), unprocessed));
          Utils.sleep(10000);
        }
      }
    }
    return unprocessed;
  }

  private final String   name;
  private final DateTime filedate;
  private DataSeriesInfo dsi;
  private DataSeries     ds;
  private boolean        update;

  /**
   * Copy constructor
   * 
   * @param toCopy Instance to copy
   */
  public FredUpdate(FredUpdate toCopy) {
    this.name = toCopy.name;
    this.filedate = new DateTime(toCopy.filedate);
    this.dsi = toCopy.dsi;
    this.ds = null;
    this.update = toCopy.update;
  }

  /**
   * Constructor
   *
   * @param name     Name of code
   * @param filedate Date of file found on system.
   */
  public FredUpdate(String name, DateTime filedate) {
    this.name = name;
    this.filedate = new DateTime(filedate);
    this.dsi = null;
    this.ds = null;
    this.update = false;
  }

  public DataSeries getDs() {
    return this.ds;
  }

  public DataSeriesInfo getDsi() {
    return this.dsi;
  }

  public String getName() {
    return this.name;
  }

  public boolean isUpdate() {
    return this.update;
  }

  @Override
  public String toString() {
    String ret;
    if (this.dsi != null) {
      ret = String.format("%s : %s  update=%s %nDSI%n%s", this.name, this.filedate, this.update, this.dsi);
    }
    else {
      ret = String.format("No DSI for %s", this.name);
    }

    if (this.ds != null) {
      ret += String.format("%nDS : %s", this.ds);
    }
    else {
      ret += String.format("%nNo DS for %s", this.name);
    }

    return ret;
  }
}
