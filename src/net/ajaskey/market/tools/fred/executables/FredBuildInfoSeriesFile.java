package net.ajaskey.market.tools.fred.executables;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.TextUtils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.FredUtils;

public class FredBuildInfoSeriesFile {

  /**
   * 
   * @param infilename
   * @param outfilename
   */
  public static void buildFromFile(String infilename, String outfilename) {

    List<DataSeriesInfo> dsiList = new ArrayList<>();

    DateTime dt = new DateTime(2000, DateTime.JANUARY, 1);

    final Set<String> uniqCodes = new HashSet<>();

    List<String> lines = TextUtils.readTextFile(infilename, true);
    for (String line : lines) {
      String fld[] = line.trim().split("\\s+");
      String name = fld[0].trim().toUpperCase();
      uniqCodes.add(name);

    }
    List<String> codes = new ArrayList<>(uniqCodes);
    Collections.sort(codes);

    for (String code : codes) {
      DataSeriesInfo dsi = new DataSeriesInfo(code, dt);
      if (dsi.isValid()) {
        dsiList.add(dsi);
      }
    }

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

    List<DataSeriesInfo> dsiList = new ArrayList<>();

    final Set<String> uniqCodes = new HashSet<>();

    final File folder = new File(fredlibdir);
    final File[] existingFiles = folder.listFiles();
    for (File f : existingFiles) {
      String name = f.getName();
      if (!name.startsWith("[")) {
        uniqCodes.add(name.replaceAll(".csv", ""));
      }
    }
    List<String> codes = new ArrayList<>(uniqCodes);
    Collections.sort(codes);

    DateTime dt = new DateTime(2000, DateTime.JANUARY, 1);

    for (String code : codes) {
      DataSeriesInfo dsi = new DataSeriesInfo(code, dt);
      if (dsi.isValid()) {
        dsiList.add(dsi);
      }
    }

    try {
      FredUtils.writeSeriesInfoCsv(dsiList, outfilename);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * For simple testing.
   * 
   * @param args
   */
  public static void main(String[] args) {

    ApiKey.set();

    Debug.init("debug/FredBuildInfoSeriesFile.dbg");

    // buildFromDir("D:\\FRED-Data\\processed",
    // "D:\\FRED-Data\\out\\fred-series-info-fromdir.csv");

    buildFromFile("D:\\FRED-Data\\input\\fred-series-info.txt", "D:\\FRED-Data\\out\\fred-series-info-fromfile.csv");

  }

}
