package net.ajaskey.market.tools.fred.queries;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.TextUtils;

/**
 * 
 * @author Computer
 *
 */
public class ParseFredSeriesData {

  private static DateTime usefulDate = new DateTime(2022, DateTime.OCTOBER, 1);

  public static void main(String[] args) throws FileNotFoundException {

    final List<String> relList = ParseFredSeriesData.getReleasesToProcess("FredSeries/release-list.txt");

    final List<ParseFredSeriesData> pdsList = new ArrayList<>();

    for (final String fname : relList) {

      System.out.printf("release : %s%n", fname);

      final List<String> data = TextUtils.readTextFile("FredSeries/" + fname, false);

      final String header = data.get(0).trim();

      String filename = header.replaceAll(" : ", "").replaceAll("\"", "");
      try (PrintWriter pw = new PrintWriter("out/" + filename + ".csv")) {

        for (int i = 1; i < data.size(); i++) {
          final String s = data.get(i);
          System.out.printf("filename : %s", filename);

          final ParseFredSeriesData pds = new ParseFredSeriesData(s, header);
          System.out.println(pds.getTitle());

          if (pds.isValid()) {

            if (pds.getTitle().equals("Retail Sales")) {
              System.out.println(pds);
            }

            if (pds.isUseful()) {
              pdsList.add(pds);
              pw.println(pds.getName());
            }
          }
        }
      }
    }

    try (PrintWriter pw = new PrintWriter("debug/ParseFredSeriesData.dbg")) {
      for (final ParseFredSeriesData pds : pdsList) {
        pw.println(pds);
      }
    }
    System.out.println(pdsList.size());
  }

  /**
   *
   * @param filename
   * @return
   */
  private static List<String> getReleasesToProcess(String filename) {
    final List<String> list = TextUtils.readTextFile(filename, false);
    return list;
  }

  private String   name;
  private String   title;
  private String   seasonality;
  private String   frequency;
  private DateTime lastUpdate;
  private String   release;
  private boolean  valid;

  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

  public ParseFredSeriesData(String data, String rel) {
    if (data.length() > 209) {
      String stmp = data.substring(0, 40);
      this.name = stmp.trim();
      stmp = data.substring(40, 176);
      this.title = stmp.trim();
      stmp = data.substring(176, 180);
      this.seasonality = stmp.trim();
      stmp = data.substring(181, 191);
      this.frequency = stmp.trim();
      stmp = data.substring(192, 210);
      this.lastUpdate = new DateTime(stmp.trim(), this.sdf);
      this.release = rel;
      this.valid = true;
    }
    else {
      this.valid = false;
    }
  }

  public String getFrequency() {
    return this.frequency;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  public String getName() {
    return this.name;
  }

  public String getSeasonality() {
    return this.seasonality;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += String.format("Name          : %s%n", this.name);
    ret += String.format("  Title       : %s%n", this.title);
    ret += String.format("  Seasonality : %s%n", this.seasonality);
    ret += String.format("  Frequency   : %s%n", this.frequency);
    ret += String.format("  Last Update : %s%n", this.lastUpdate);
    ret += String.format("  Release     : %s", this.release);

    return ret;
  }

  /**
   *
   * @return
   */
  private boolean isUseful() {
    if (!this.seasonality.equalsIgnoreCase("SA")) {
      if (!this.frequency.equalsIgnoreCase("Quarterly")) {
        if (!this.frequency.equalsIgnoreCase("Annual")) {
          if (!this.title.toLowerCase().contains("discontinued")) {
            if (!this.title.toLowerCase().contains("northeast")) {
              if (!this.title.toLowerCase().contains("midwest")) {
                if (!this.title.toLowerCase().contains("south census")) {
                  if (!this.title.toLowerCase().contains("west census")) {
                    if (!this.title.toLowerCase().contains("central census")) {
                      if (!this.title.toLowerCase().contains("atlantic census")) {
                        if (!this.title.toLowerCase().contains("mountain census")) {
                          if (!this.title.toLowerCase().contains("pacific census")) {
                            if (!this.title.toLowerCase().contains("england census")) {
                              if (!this.title.contains("Establishments")) {
                                if (this.lastUpdate.isGreaterThanOrEqual(ParseFredSeriesData.usefulDate)) {

                                  return true;
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

}
