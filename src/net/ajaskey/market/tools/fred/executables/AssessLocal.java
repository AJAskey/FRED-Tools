package net.ajaskey.market.tools.fred.executables;

import java.io.File;
import java.util.List;

import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.LocalFormat;

public class AssessLocal {

  final static String fredlib = "D:/data2/MA/CSV Data/FRED-Download";

  public static void main(String[] args) {

    final List<LocalFormat> bigList = LocalFormat.readSeriesList("input/MasterSeriesSummary.txt", fredlib);

    List<File> files = Utils.getDir(fredlib, "csv");

    for (File f : files) {
      if (!f.getName().startsWith("[")) {
        String seriesId = f.getName().replaceAll(".csv", "");
        LocalFormat lf = LocalFormat.findInList(seriesId, bigList);
        if (!lf.isValid()) {
          System.out.println(lf.getId());
        }
      }
    }

  }

}
