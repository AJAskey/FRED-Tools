package net.ajaskey.market.tools.fred;

import java.io.File;

import net.ajaskey.common.DateTime;

public class LocalFormat {

  private String   name;
  private String   title;
  private String   seasonality;
  private String   frequency;
  private DateTime lastUpdate;
  String           lastUpdateStr;
  private String   release;
  private File     localFile;
  private DateTime localFileDate;
  private boolean  valid;

  public LocalFormat() {
    // TODO Auto-generated constructor stub
  }

  public static LocalFormat parseline(String line, String fredlib) {

    LocalFormat ret = new LocalFormat();

    final int len = line.length();
    if (len > 180) {

      String stmp = line.substring(0, 30);
      ret.name = stmp.trim();
      stmp = line.substring(30, 151);
      ret.title = stmp.trim();
      stmp = line.substring(151, 155);
      ret.seasonality = stmp.trim();
      stmp = line.substring(156, 167);
      ret.lastUpdateStr = stmp;
      // ret.lastUpdate = new DateTime(stmp.trim(), ret.sdf);
      stmp = line.substring(168);
      ret.frequency = stmp.trim();
      // ret.release = rel;

      ret.localFile = new File(String.format("%s/%s.csv", fredlib, ret.name));

      if (ret.localFile.exists()) {
        ret.localFileDate = new DateTime(ret.localFile.lastModified());
      }
      else {
        ret.localFile = null;
        ret.localFileDate = null;
      }

      ret.valid = true;
    }
    else {
      ret.valid = false;
    }

    return ret;
  }

  public String formatline() {
    return "";
  }

  public String getName() {
    return name;
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

  public String getLastUpdateStr() {
    return lastUpdateStr;
  }

  public String getRelease() {
    return release;
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

}
