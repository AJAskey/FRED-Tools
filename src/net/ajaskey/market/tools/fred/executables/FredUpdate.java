package net.ajaskey.market.tools.fred.executables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.FredUtils;

public class FredUpdate {

  private String         name;
  private File           file;
  private DateTime       filedate;
  private DataSeriesInfo dsi;
  private DataSeries     ds;

  public FredUpdate(String name) {
    this.name = name;
    this.file = null;
    this.filedate = null;
    this.dsi = null;
    this.ds = null;
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {

    ApiKey.set();

    Debug.init("debug/FredInitLibrary.dbg");

    Utils.makeDir(FredUtils.fredPath + "/processed");
    Utils.makeDir(FredUtils.fredPath + "/out");

    final List<String> codeNames = FredUtils.readSeriesList(FredUtils.fredPath + "/input/fred-series-info.txt");
    List<FredUpdate> fuList = new ArrayList<>();

    for (String code : codeNames) {
      FredUpdate fu = new FredUpdate(code);
      fuList.add(fu);
      System.out.println(code);
    }
    System.out.println(codeNames.size());

    DateTime dt = new DateTime();

    for (FredUpdate fu : fuList) {
      DataSeriesInfo dsi = new DataSeriesInfo(fu.getName(), dt);
      if (dsi.isValid()) {
        fu.dsi = dsi;
        fu.file = new File(FredUtils.fredPath + "/processed/" + dsi.getName() + ".csv");
        if (fu.file.exists()) {
          fu.filedate = new DateTime(fu.file.lastModified());
        }
        else {
          fu.filedate = new DateTime(2000, DateTime.JANUARY, 1);
        }
        System.out.println(fu);
      }
    }
  }

  @Override
  public String toString() {
    String ret = String.format("%s : %s%n%s", this.name, this.filedate, this.dsi);
    return ret;
  }

  public String getName() {
    return name;
  }

  public DataSeriesInfo getDsi() {
    return dsi;
  }

  public DataSeries getDs() {
    return ds;
  }

}
