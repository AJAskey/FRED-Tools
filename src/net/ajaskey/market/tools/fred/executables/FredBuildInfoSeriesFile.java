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
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.DsiQuery;
import net.ajaskey.market.tools.fred.FredUtils;

/**
 * This class provides methods to create file <b>fred-series-info.txt</b>
 * containing the latest data available for download from FRED
 */
public class FredBuildInfoSeriesFile {

  /**
   * Reads a file containing FRED series id names, queries FRED for the latest
   * info, and then writes that data.
   * 
   * @param infilename  Name of file containing series id names
   * @param outfilename Name of file to write the output
   */
  public static void buildFromFile(String infilename, String outfilename) {

    List<DataSeriesInfo> dsiList = DsiQuery.queryDataSeriesInfo(infilename);

    try {
      FredUtils.writeSeriesInfoCsv(dsiList, outfilename);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param fredlibdir
   * @param outfilename
   */
  public static void buildFromDir(String fredlibdir, String outfilename) {

    List<DataSeriesInfo> dsiList = DsiQuery.queryDataSeriesInfo(new File(fredlibdir));

    try {
      FredUtils.writeSeriesInfoCsv(dsiList, outfilename);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }

  }

  public static List<DataSeriesInfo> queryDsi(String infile) {
    return null;

  }

  /**
   * For simple testing.
   * 
   * @param args
   */
  public static void main(String[] args) {

    Utils.makeDir("debug");
    Utils.makeDir("out");
    ApiKey.set();

    Debug.init("debug/FredBuildInfoSeriesFile.dbg", java.util.logging.Level.INFO);

//    List<DataSeriesInfo> dsiList = DsiQuery.queryDataSeriesInfo("data/fred-series-info-test.txt");
//    for (DataSeriesInfo dsi : dsiList) {
//      System.out.println(dsi);
//    }

    buildFromDir("data", "out/fred-series-info-fromdir.csv");

    // buildFromFile("data/fred-series-info-test.txt",
    // "out/fred-series-info-fromfile.csv");

  }

}
