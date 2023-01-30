package net.ajaskey.market.tools.fred;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.TextUtils;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.queries.Series;

public class LocalFormat {

  public class LfSorter implements Comparator<LocalFormat> {

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final LocalFormat lf1, final LocalFormat lf2) {

      if (lf1 == null || lf2 == null) {
        return 0;
      }

      try {
        int ret = 0;
        if (lf1.getLastObservation().isGreaterThan(lf2.getLastObservation())) {
          ret = -1;
        }
        else if (lf1.getLastObservation().isLessThan(lf2.getLastObservation())) {
          ret = 1;
        }
        return ret;
      }
      catch (final Exception e) {
        return 0;
      }
    }
  }

  public class LfAbcSorter implements Comparator<LocalFormat> {

    /*
     * (non-Javadoc)
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(final LocalFormat lf1, final LocalFormat lf2) {

      if (lf1 == null || lf2 == null) {
        return 0;
      }

      try {
        int ret = lf1.getId().compareTo(lf2.getId());
        return ret;
      }
      catch (final Exception e) {
        return 0;
      }
    }
  }

  private final static LocalFormat tmpLfClass   = new LocalFormat();
  public final static LfSorter     sorter       = tmpLfClass.new LfSorter();
  public final static LfAbcSorter  sorterAbc    = tmpLfClass.new LfAbcSorter();
  private final static String      lfDateFormat = "dd-MMM-yyyy";

  /**
   *
   * @param id
   * @param list
   * @return
   */
  public static LocalFormat findInList(String id, List<LocalFormat> list) {

    for (final LocalFormat lf : list) {
      if (lf.getId().equalsIgnoreCase(id)) {
        return lf;
      }
    }

    return null;
  }

  /**
   * 
   * @param filename
   * @param fredlib
   * @param isInFredLib
   * @return
   */
  public static List<LocalFormat> readMasterList(String filename, String fredlib, boolean isInFredLib) {

    final List<LocalFormat> lfList = new ArrayList<>();

    final List<String> lfData = TextUtils.readTextFile(filename, false);

    for (final String s : lfData) {
      final LocalFormat lf = LocalFormat.build(s.trim(), fredlib);
      boolean doAdd = false;
      if (lf.isValid()) {
        if (isInFredLib && lf.localFileLastObs != null) {
          doAdd = true;
        }
        else if (!isInFredLib) {
          doAdd = true;
        }
        if (doAdd) {
          lfList.add(lf);
        }
      }
    }

    return lfList;
  }

  /**
   *
   * @param releaseId
   * @param seriesLib
   * @param fredlib
   * @return
   */
  public static List<LocalFormat> readReleaseSeriesInfo(String releaseId, String seriesLib, String fredlib) {

    final List<LocalFormat> lfList = new ArrayList<>();

    final String fname = String.format("%s/%s.txt", seriesLib, releaseId);
    final List<String> data = TextUtils.readTextFile(fname, false);

    if (data.size() > 1) {
      for (int i = 1; i < data.size(); i++) {
        final LocalFormat lf = new LocalFormat(data.get(0), fredlib);
        lf.parseline(data.get(i));
        if (lf.isValid()) {
          if (lf.localFile != null) {
            // Update local file date to actual
            if (lf.localFile.exists()) {
              lf.setLocalFileLastObs();
            }
            else {
              lf.localFileLastObs = null;
            }
          }
          lfList.add(lf);
        }
      }
    }
    return lfList;
  }

  /**
   *
   * @param seriesLib
   * @param fredlib
   * @return
   */
  public static List<LocalFormat> readReleaseSeriesInfoDir(String seriesLib, String fredlib) {

    final List<LocalFormat> lfList = new ArrayList<>();

//    final String[] ext = { "txt" };
    final List<File> files = Utils.getDir(seriesLib, "txt");

    for (final File f : files) {
      final String relId = f.getName().replaceAll(seriesLib, "").replaceAll(".txt", "");
      if (relId.startsWith("Id")) {
        System.out.println("Processing Release : " + relId);
        final List<LocalFormat> list = LocalFormat.readReleaseSeriesInfo(relId, seriesLib, fredlib);
        lfList.addAll(list);
      }
    }

    return lfList;
  }

  /**
   * 
   * @param filename
   * @param fredlib
   * @param bigList
   * @return
   */
  public static List<LocalFormat> readRawList(String filename, final List<LocalFormat> bigList) {

    final List<LocalFormat> lfList = new ArrayList<>();
    final List<String> data = TextUtils.readTextFile(filename, false);

    for (String s : data) {
      if (s.length() > 0) {
        String fld[] = s.split("\\s+");
        try {
          LocalFormat lf = findInList(fld[0].trim(), bigList);
          if (lf != null) {
            if (lf.isValid()) {
              lfList.add(lf);
            }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    return lfList;
  }

  /**
   * 
   * @param filename
   * @param fredlib
   * @return
   */
  public static List<LocalFormat> readSeriesList(String filename, String fredlib) {

    final List<LocalFormat> lfList = new ArrayList<>();

    final List<String> data = TextUtils.readTextFile(filename, false);

    for (final String s : data) {
      final LocalFormat lf = new LocalFormat("999\tdummy", fredlib);
      lf.parseline(s);
      if (lf.isValid()) {
        // Update local file date to actual
        if (lf.localFile != null) {
          if (lf.localFile.exists()) {
            lf.setLocalFileLastObs();
          }
          else {
            lf.localFileLastObs = null;
          }
        }
        lfList.add(lf);
      }
    }
    return lfList;
  }

  /**
   * 
   * @param data
   * @param fredlib
   * @return
   */
  private static LocalFormat build(String data, String fredlib) {
    final LocalFormat lf = new LocalFormat();
    lf.fredlib = fredlib;
    lf.parseline(data);
    return lf;
  }

  private String   id;
  private String   title;
  private String   seasonality;
  private String   frequency;
  private String   units;
  private DateTime lastUpdate;
  private DateTime lastObservation;
  private String   releaseId;
  private String   releaseName;
  private File     localFile;
  private DateTime localFileLastObs;
  private String   filename;
  private String   fredlib;
  private boolean  valid;

  public LocalFormat() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Constructor used for Series data queried from FRED
   *
   * @param series       Series data queries from FRED
   * @param release_id   Release Id pointing to the Series data at FRED
   * @param release_name Release Name pointing the the Series data at FRED
   * @param fredlib      Directory containing the files containing the most recent
   *                     date/value pairs downloaded from FRED.
   */
  public LocalFormat(Series series, String release_id, String release_name, String fredlib) {
    this.id = series.getId();
    this.title = series.getTitle();
    this.seasonality = series.getSeasonalAdjustmentShort();
    this.frequency = series.getFrequency();
    this.units = series.getUnits();
    this.lastUpdate = new DateTime(series.getLastUpdate());
    this.lastObservation = new DateTime(series.getLastObservation());
    this.releaseId = release_id;
    this.releaseName = release_name;
    this.fredlib = fredlib;
    final String tmp = String.format("%s/%s.csv", this.fredlib, this.id);
    this.localFile = new File(tmp);
    if (this.localFile.exists()) {
      this.setLocalFileLastObs();
    }
  }

  /**
   * Constructor use for data read from files
   *
   * @param release Header from data file containing released id and name
   * @param fredlib Directory containing the files containing the most recent
   *                date/value pairs downloaded from FRED.
   */
  public LocalFormat(String release, String fredlib) {
    final String fld[] = release.split("\t");
    final int idx = fld[0].indexOf(':');
    final String id = fld[0].substring(idx + 1).trim();
    final String name = fld[1].trim();
    this.releaseId = id.trim();
    this.releaseName = name.trim();
    this.fredlib = fredlib;
    this.filename = String.format("Id%s %s", this.releaseId, this.releaseName);
    final String tmp = String.format("%s/%s.csv", this.fredlib, this.id);
    this.localFile = new File(tmp);
    if (this.localFile.exists()) {
      this.setLocalFileLastObs();
    }
  }

  /**
   * Formats a String matching the format read by the <b>parseline()</b> procedure
   *
   * @return A formatted String of the class data
   */
  public String formatline() {

    String t = this.getTitle().trim();
    if (t.length() > 120) {
      t = t.substring(0, 119);
    }

    String freq = this.getFrequency().trim();
    if (freq.length() > 19) {
      freq = freq.substring(0, 18);
    }

    String lflo = " ";
    if (this.localFileLastObs != null) {
      lflo = this.localFileLastObs.format(lfDateFormat);
    }

    String lo = this.lastObservation.format(lfDateFormat);

    String scaler = " ";
    final String unt = this.units.trim().toLowerCase();
    if (unt.length() == 1) {
      scaler = this.units;
    }
    else if (unt.contains("billion")) {
      scaler = "B";
    }
    else if (unt.contains("million")) {
      scaler = "M";
    }
    else if (unt.contains("thousand")) {
      scaler = "T";
    }
    else if (unt.contains("percent") || unt.equalsIgnoreCase("rate")) {
      scaler = "P";
    }
    else if (unt.equalsIgnoreCase("ratio")) {
      scaler = "R";
    }

    final String sum = String.format("%-30s%-120s %1s %-4s %-11s %-11s %-11s %-19s", this.getId(), t, scaler, this.getSeasonality(),
        this.getLastUpdate(), lo, lflo, freq);

    return sum;
  }

  public String formatlineRel() {

    String s = formatline();
    String ret = String.format("%s  %-3s %-10s", s, this.releaseId, this.releaseName);

    return ret;

  }

  public String getFilename() {
    return this.filename;
  }

  public String getFrequency() {
    return this.frequency;
  }

  public String getId() {
    return this.id;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  public File getLocalFile() {
    return this.localFile;
  }

  public DateTime getLocalFileLastObs() {
    return this.localFileLastObs;
  }

  public DateTime getLastObservation() {
    return this.lastObservation;
  }

  public String getReleaseId() {
    return this.releaseId;
  }

  public String getReleaseName() {
    return this.releaseName;
  }

  public String getSeasonality() {
    return this.seasonality;
  }

  public String getTitle() {
    return this.title;
  }

  public String getUnits() {
    return this.units;
  }

  public boolean isValid() {
    return this.valid;
  }

  /**
   * Parse the fixed field length line from previous output data
   *
   * @param line String to process
   */
  public void parseline(String line) {

    try {

      final int len = line.length();
      if (len > 180) {

        String stmp = line.substring(0, 29);
        this.id = stmp.trim();
        stmp = line.substring(30, 151);
        this.title = stmp.trim();
        stmp = line.substring(151, 152);
        this.units = stmp.trim();
        stmp = line.substring(153, 157);
        this.seasonality = stmp.trim();

        stmp = line.substring(158, 169);
        this.lastUpdate = new DateTime(stmp, lfDateFormat);

        stmp = line.substring(170, 181);
        this.lastObservation = new DateTime(stmp, lfDateFormat);

        stmp = line.substring(182, 193);
        this.localFileLastObs = new DateTime(stmp, lfDateFormat);

        stmp = line.substring(194, 213);
        this.frequency = stmp.trim();

        if (len > 214) {
          stmp = line.substring(214);
          this.releaseId = stmp.substring(0, 3).trim();
          this.releaseName = stmp.substring(4).trim();
        }

        this.localFile = new File(String.format("%s/%s.csv", this.fredlib, this.id));

        if (this.localFile.exists()) {
          this.setLocalFileLastObs();
        }
        else {
          this.localFile = null;
          this.localFileLastObs = null;
        }

        this.valid = true;
      }
      else {
        this.valid = false;
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
      this.valid = false;
    }
  }

  public void setId(String id, String fredlib) {
    this.id = id;
    this.fredlib = fredlib;

    final String tmp = String.format("%s/%s.csv", this.fredlib, this.id);
    this.localFile = new File(tmp);
    this.setLocalFileLastObs();
  }

  @Override
  public String toString() {
    String ret = "Id              : " + this.id + Utils.NL;
    ret += "Title           : " + this.title + Utils.NL;
    ret += "Seasonality     : " + this.seasonality + Utils.NL;
    ret += "Frequency       : " + this.frequency + Utils.NL;
    ret += "Units           : " + this.units + Utils.NL;
    ret += "LastUpdate      : " + this.lastUpdate + Utils.NL;
    ret += "LastObservation : " + this.lastObservation + Utils.NL;
    ret += "Release         : " + this.releaseId + Utils.TAB + this.releaseName + Utils.NL;
    if (this.localFileLastObs != null) {
      ret += "Local File      : " + this.localFile + Utils.TAB + this.localFileLastObs.format("dd-MMM-yyyy") + Utils.NL;
    }
    ret += "Valid           : " + this.valid;
    return ret;
  }

  public void setLocalFileLastObs() {
    if (this.localFile.exists()) {
      this.localFileLastObs = FredUtils.getLastObservation(this.localFile);
    }
    else {
      this.localFileLastObs = null;
    }
  }

  public void update(DateTime lupdate, DateTime lobs) {
    this.lastUpdate = new DateTime(lupdate);
    this.lastObservation = new DateTime(lobs);

  }

}
