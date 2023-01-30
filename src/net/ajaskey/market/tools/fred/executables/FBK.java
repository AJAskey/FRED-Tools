package net.ajaskey.market.tools.fred.executables;

import java.io.File;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.TextUtils;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.FredUtils;
import net.ajaskey.market.tools.fred.LocalFormat;

public class FBK {

  final private static String fredlib = "D:/data2/MA/CSV Data/FRED-Download";
  final private static String lfIds   = "input/filteredSeriesSummary.txt";

  public static void main(String[] args) {

    final List<LocalFormat> lfList = LocalFormat.readSeriesList(lfIds, fredlib);

    List<File> files = Utils.getDir(fredlib, "csv");

    for (File f : files) {
      String fname = f.getName();
      if (!fname.startsWith("[")) {
        String name = fname.replaceAll(".csv", "");

        LocalFormat lf = LocalFormat.findInList(name, lfList);
        if (lf != null) {
          if (!lf.getFrequency().toLowerCase().contains("daily")) {

            DateTime dt = getLastObservation(f);
            System.out.printf("%-20s %-11s %s%n", name, dt.format("yyyy-MMM-dd"), lf.getFrequency());
          }
        }
        else {
          System.out.println("NO LF found for " + fname);
        }
      }
    }
  }

  public static DateTime getLastObservation(File f) {

    final List<String> data = TextUtils.readTextFile(f, false);
    String s = data.get(data.size() - 1);
    String ss[] = s.split(",");
    DateTime dt = new DateTime(ss[0].trim(), FredUtils.optumaDateFormat);
    return dt;
  }

}
