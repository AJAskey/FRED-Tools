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

  private static DateTime usefulDate = new DateTime(2022, DateTime.SEPTEMBER, 1);

  public static void main(String[] args) throws FileNotFoundException {

    final List<String> relList = ParseFredSeriesData.getReleasesToProcess("FredSeries/release-list.txt");

    setUsefulList();

    final List<ParseFredSeriesData> pdsList = new ArrayList<>();

    try (PrintWriter pwAll = new PrintWriter("out/filteredSeriesSummary.txt")) {

      int processed = 0;

      /**
       * Process each Release from list.
       */
      for (final String fname : relList) {

        System.out.printf("release : %s", fname);

        String fileToProcess = "FredSeries/" + fname;
        final List<String> data = TextUtils.readTextFile(fileToProcess, false);

        final String header = data.get(0).trim();

        String filename = header.replaceAll(" : ", "").replaceAll("\"", "");
        /**
         * Files to be read into Optuma as a list of charts.
         */
        try (PrintWriter pw = new PrintWriter("optuma/" + filename + ".csv")) {

          for (int i = 1; i < data.size(); i++) {

            final String s = data.get(i);

            final ParseFredSeriesData pds = new ParseFredSeriesData(s, header);

            if (pds.isValid()) {

              if (pds.isUseful()) {
                pdsList.add(pds);
                pw.printf("%s,%s,%s,%s%n", pds.getName(), pds.getSeasonality(), pds.getFrequency(), pds.getTitle());

                String sDate = pds.lastUpdate.format("dd-MMM-yyyy");

                processed++;
                pwAll.printf("%-30s %-4s  %-11s  %-30s %-120s %s%n", pds.getName(), pds.getSeasonality(), sDate, pds.getFrequency(), pds.getTitle(),
                    data.get(0).trim());
              }
            }
          }
        }
        System.out.printf("   %d%n", processed);
        processed = 0;
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
  String           lastUpdateStr;
  private String   release;
  private boolean  valid;

  SimpleDateFormat sdf    = new SimpleDateFormat("dd-MMM-yyyy");
  SimpleDateFormat sdfout = new SimpleDateFormat("dd-MMM-yyyy");

  public ParseFredSeriesData(String data, String rel) {
    int len = data.length();
    if (len > 180) {

      String stmp = data.substring(0, 40);
      this.name = stmp.trim();
      stmp = data.substring(40, 161);
      this.title = stmp.trim();
      stmp = data.substring(161, 165);
      this.seasonality = stmp.trim();
      stmp = data.substring(166, 177);
      this.lastUpdateStr = stmp;
      this.lastUpdate = new DateTime(stmp.trim(), this.sdf);
      stmp = data.substring(178);
      this.frequency = stmp.trim();

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
    ret += String.format("  Last Update : %s     %s%n", this.lastUpdate, this.lastUpdateStr);
    ret += String.format("  Release     : %s", this.release);

    return ret;
  }

  /**
   *
   * @return
   */
  private boolean isUseful() {

//    if (!this.name.equals("xyzxcv")) {
//      return true;
//    }

    if (isInUsefulList(this.name)) {
      return true;
    }

    if (this.title.toUpperCase().contains("DISCONTINUED")) {
      return false;
    }

    if (this.lastUpdate.isLessThan(usefulDate)) {
      return false;
    }

    // Add in Industrial Production NSA Monthly
    if (this.name.startsWith("IP")) {
      if (this.frequency.equals("Monthly")) {
        if (this.seasonality.equals("NSA")) {
          return true;
        }
        else {
          return false;
        }
      }
      else {
        return false;
      }
    }

    // Filter Employment Situation
    if (this.release.contains("Employment Situation")) {
      if (this.title.startsWith("All Employees")) {
        if (this.seasonality.equals("NSA")) {
          return true;
        }
      }
      return false;
    }

    // Add in Capacity Utilization SA Monthly
    if (this.name.startsWith("CAP")) {
      if (this.frequency.equals("Monthly")) {
        return true;
      }
    }
    // Add GDPNow Release series
    if (this.release.contains("GDPNow")) {
      return true;
    }

    // Filter Id : 53 Gross Domestic Product. Desired are in useful list.
    if (this.release.contains("Gross Domestic Product")) {
      return false;
    }

    // Filter Id : 20 H.4.1. Desired are in useful list.
    if (this.release.contains("H.4.1")) {
      return false;
    }

    if (!this.seasonality.equalsIgnoreCase("SA")) {
      if (!this.seasonality.equalsIgnoreCase("SAAR")) {
        if (!this.frequency.equalsIgnoreCase("Not Applicable")) {
          if (!this.frequency.contains("Annual")) {
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
                                if (!this.title.contains("Employment-Population Ratio -")) {
                                  if (!this.title.contains("mployed -")) {
                                    if (!this.title.contains("Not in Labor Force -")) {
                                      if (!this.title.contains("mployment Rate -")) {
                                        if (!this.title.contains("mployment Rate:")) {
                                          if (!this.title.contains("Civilian Labor Force ")) {
                                            if (!this.title.contains("mployment Level -")) {
                                              if (!this.title.contains("Civilian Labor Force:")) {
                                                if (!this.title.contains("mployment Level:")) {
                                                  if (!this.title.contains("Population Level -")) {
                                                    if (!this.title.contains("Labor Force Participation Rate -")) {
                                                      if (!this.title.contains("Houses Sold by ")) {
                                                        if (!this.title.contains("Multiple Jobholders")) {

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

  private static List<String> usefulList = new ArrayList<>();

  private static void setUsefulList() {

    List<String> useThese = TextUtils.readTextFile("FredSeries/SeriesIdAdditions.txt", false);
    for (String s : useThese) {
      usefulList.add(s.trim());
    }
  }

  private boolean isInUsefulList(String n) {
    for (String s : usefulList) {
      if (s.equalsIgnoreCase(n)) {
        return true;
      }
    }
    return false;
  }

}
