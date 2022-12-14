package net.ajaskey.market.tools.fred;

import java.io.File;

import net.ajaskey.common.DateTime;
import net.ajaskey.market.tools.fred.queries.Series;

public class LocalFormat {

  private String   id;
  private String   title;
  private String   seasonality;
  private String   frequency;
  private DateTime lastUpdate;
  private String   releaseId;
  private String   releaseName;
  private File     localFile;
  private DateTime localFileDate;
  private String   filename;
  private String   fredlib;
  private boolean  valid;

  /**
   * Constructor use for data read from files
   * 
   * @param release Header from data file containing released id and name
   * @param fredlib Directory containing the files containing the most recent
   *                date/value pairs downloaded from FRED.
   */
  public LocalFormat(String release, String fredlib) {
    String fld[] = release.split("\t");
    int idx = fld[0].indexOf(':');
    String id = fld[0].substring(idx + 1).trim();
    String name = fld[1].trim();
    this.releaseId = id.trim();
    this.releaseName = name.trim();
    this.fredlib = fredlib;
    this.filename = String.format("Id%s %s", this.releaseId, this.releaseName);
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
    this.lastUpdate = new DateTime(series.getLastUpdate());
    this.releaseId = release_id;
    this.releaseName = release_name;
    this.fredlib = fredlib;
    String tmp = String.format("%s/%s.csv", this.fredlib, this.id);
    this.localFile = new File(tmp);
    if (this.localFile.exists()) {
      this.localFileDate = new DateTime(this.localFile.lastModified());
    }
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

        String stmp = line.substring(0, 30);
        this.id = stmp.trim();
        stmp = line.substring(30, 151);
        this.title = stmp.trim();
        stmp = line.substring(151, 155);
        this.seasonality = stmp.trim();
        stmp = line.substring(156, 167);
        this.lastUpdate = new DateTime(stmp, "dd-MMM-yyyy");
        stmp = line.substring(180);
        this.frequency = stmp.trim();

        this.localFile = new File(String.format("%s/%s.csv", this.fredlib, this.id));

        if (this.localFile.exists()) {
          this.localFileDate = new DateTime(this.localFile.lastModified());
        }
        else {
          this.localFile = null;
          this.localFileDate = null;
        }

        this.valid = true;
      }
      else {
        this.valid = false;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      this.valid = false;
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

    String f = this.getFrequency().trim();
    if (f.length() > 19) {
      f = f.substring(0, 18);
    }

    String lfd = " ";
    if (localFileDate != null) {
      lfd = localFileDate.format("dd-MMM-yyyy");
    }
    String sum = String.format("%-30s%-120s %-4s %-11s %-11s %-19s", this.getId(), t, this.getSeasonality(), this.getLastUpdate(), lfd, f);

    return sum;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getSeasonality() {
    return seasonality;
  }

  public String getFrequency() {
    return frequency;
  }

  public DateTime getLastUpdate() {
    return lastUpdate;
  }

  public String getReleaseId() {
    return releaseId;
  }

  public String getReleaseName() {
    return releaseName;
  }

  public File getLocalFile() {
    return localFile;
  }

  public DateTime getLocalFileDate() {
    return localFileDate;
  }

  public boolean isValid() {
    return valid;
  }

  public String getFilename() {
    return filename;
  }

}
