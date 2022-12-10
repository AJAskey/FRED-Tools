package net.ajaskey.market.tools.fred.queries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

public class PullAll {

  static public String seriesOutputFormat = "%-40s%-120s %-4s %-12s %s";

  static List<String> summary = new ArrayList<>();

  static PrintWriter allSeriesPw = null;

  /**
   * 
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {

    processAll("FredLib");
  }

  /**
   * 
   * @param id
   */
  public static void processReleaseId(String id) {

    Release rel = new Release(id);

  }

  /**
   * 
   * @param fredlib
   * @throws FileNotFoundException
   */
  public static void processAll(String fredlib) throws FileNotFoundException {

    Debug.init("debug/PullAll.dbg", java.util.logging.Level.INFO);

    allSeriesPw = new PrintWriter("out/allSeriesSummary.txt");

    ApiKey.set();

    List<Release> relList = Release.queryReleases();

    int lastMoreToDo = 9999999;
    while (relList.size() > 0) {

      List<Release> moreToDo = process(relList, 5, 7, fredlib);

      relList.clear();

      if (lastMoreToDo > moreToDo.size()) {
        lastMoreToDo = moreToDo.size();

        if (moreToDo.size() > 0) {
          relList.addAll(moreToDo);
        }
      }
    }

    allSeriesPw.close();

    Collections.sort(summary);
    try (PrintWriter pw = new PrintWriter("out/FRED-Release-Summary.txt")) {
      pw.println("Id\tName\tSeries Count");
      for (String s : summary) {
        pw.println(s);
      }
    }

  }

  /**
   * 
   * @param relList
   * @param retries
   * @param delay
   * @param fredlib
   * @return
   * @throws FileNotFoundException
   */
  private static List<Release> process(List<Release> relList, int retries, int delay, String fredlib) throws FileNotFoundException {

    List<Release> redoList = new ArrayList<>();

    for (Release rel : relList) {

      String cleanname = rel.getName().replaceAll("/", "").replaceAll(":", "").replaceAll("\\.", "_").replaceAll("\"", "").replaceAll("&", "_");
      String tmp = String.format("tmp/Id_%s%s.txt", rel.getId(), cleanname);
      String fn = tmp.replaceAll(" ", "");
      String fncsv = tmp.replaceAll(".txt", ".csv");

      File f = new File(fn);
      if (f.exists()) {
        f.delete();
      }

      try (PrintWriter pw = new PrintWriter(fn)) {

        String s = String.format("Id : %s  %s", rel.getId(), rel.getName());
        pw.println(s);

        List<Series> serList = Series.querySeriesPerRelease(rel.getId(), retries, delay, "FredLib");

        System.out.println(String.format("Processed querySeriesPerRelease for %-30s %5d %-120s %s", rel.getId(), serList.size(), rel.getName(), fn));

        if (serList.size() > 0) {
          for (Series ser : serList) {

            String t = ser.getTitle().trim();
            if (t.length() > 120) {
              t = t.substring(0, 119);
            }

            String sum = String.format(seriesOutputFormat, ser.getId(), t, ser.getSeasonalAdjustmentShort(), ser.getLastUpdate(), ser.getFrequency());
            pw.println(sum);

            allSeriesPw.printf("%s %-5s %-50s%n", sum, rel.getId(), rel.getName());
            String relsum = String.format("%s\t%s\t%d", rel.getId(), rel.getName(), serList.size());
            summary.add(relsum);
          }
        }
        else {
          pw.println("No data returned from FRED!");
          redoList.add(rel);
          Utils.sleep(15000);
        }
      }
    }
    return redoList;
  }

}
