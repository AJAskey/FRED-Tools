package net.ajaskey.market.tools.fred.queries;

import java.io.File;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeries.ResponseType;
import net.ajaskey.market.tools.fred.FredUtils;

public class Series {

  public final static SimpleDateFormat        sdf       = new SimpleDateFormat("yyyy-MM-dd");
  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  private static List<Series> seriesList = new ArrayList<>();
  private static Set<String>  uniqSeries = new HashSet<>();

  public static List<Series> querySeriesPerRelease(String release_id, int retries, int delay) {

    seriesList.clear();

    int offset = 0;
    boolean readmore = true;
    while (readmore) {
      Debug.LOGGER.info(String.format("%n---------%ncalling fredSeriesPerSeriesQuery.  release_id=%s  offset=%d", release_id, offset));
      final int num = fredSeriesPerSeriesQuery(release_id, offset, retries, delay);
      if (num < 1000) {
        readmore = false;
      }
      else {
        offset += num;
      }
    }

    return seriesList;
  }

  /**
   *
   * @param series_id
   * @return
   */
  private static int fredSeriesPerSeriesQuery(String release_id, int offset, int retries, int delay) {

    int totalProcessed = 0;

    try {

      String url = String.format("https://api.stlouisfed.org/fred/release/series?release_id=%s&api_key=%s&offset=%d", release_id, ApiKey.get(),
          offset);

      String resp = Utils.getFromUrl(url, retries, delay);

      if (resp.length() > 0) {

        if (Series.dBuilder == null) {
          Series.dBuilder = Series.dbFactory.newDocumentBuilder();
        }

        final Document doc = Series.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");

        totalProcessed = nResp.getLength();

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
            final Element eElement = (Element) nodeResp;

            Series ser = new Series();
            ser.setUrl(url);

            ser.setId(eElement.getAttribute("id").trim());
            ser.setTitle(eElement.getAttribute("title").trim());
            ser.setFrequency(eElement.getAttribute("frequency").trim());
            ser.setUnits(eElement.getAttribute("units").trim());
            ser.setType(ResponseType.LIN);
            ser.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment").trim());
            ser.setSeasonalAdjustmentShort(eElement.getAttribute("seasonal_adjustment_short").trim());
            ser.setLastUpdate(eElement.getAttribute("last_updated").trim());
            ser.setFirstObservation(eElement.getAttribute("observation_start").trim());
            ser.setLastObservation(eElement.getAttribute("observation_end").trim());
            ser.setFileDate(null);
            if (ser.title.length() > 0) {
              ser.setFullfilename(FredUtils.toFullFileName(ser.id, ser.title));
            }
            ser.notes = eElement.getAttribute("notes").trim();

            final boolean newSer = uniqSeries.add(ser.id);
            if (newSer) {
              ser.setValid(true);
              Debug.LOGGER.info(String.format("Adding series data. %s", ser.getId()));

              seriesList.add(ser);
            }
            else {
              Debug.LOGGER.info(String.format("Duplicate series not added!%s", ser.getId()));
              totalProcessed--;
            }
          }
        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    Debug.LOGGER.info(String.format("Returning from fredSeriesPerSeriesQuery - totalProcessed=%d", totalProcessed));
    return totalProcessed;
  }

  /**
   *
   * @param release_id
   * @return
   */
//  public static List<Series> querySeriesPerRelease(String release_id, int offset) {
//
//    ApiKey.set();
//
//    final List<Series> serList = new ArrayList<>();
//
//    try {
//
//      final String url = String.format("https://api.stlouisfed.org/fred/release/series?release_id=%s&api_key=%s&offset=%d", release_id, ApiKey.get(),
//          offset);
//
//      final String resp = Utils.getFromUrl(url);
//
//      if (resp.length() > 0) {
//
//        // Debug.LOGGER.info(resp + Utils.NL);
//
//        if (Series.dBuilder == null) {
//          Series.dBuilder = Series.dbFactory.newDocumentBuilder();
//        }
//
//        final Document doc = Series.dBuilder.parse(new InputSource(new StringReader(resp)));
//
//        doc.getDocumentElement().normalize();
//
//        final NodeList nResp = doc.getElementsByTagName("series");
//
//        for (int knt = 0; knt < nResp.getLength(); knt++) {
//
//          final Node nodeResp = nResp.item(knt);
//
//          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
//
//            final Element eElement = (Element) nodeResp;
//
//            final Series ser = new Series();
//
//            ser.setResponse(resp.trim());
//            ser.setUrl(url);
//            ser.setId(eElement.getAttribute("id").trim());
//            ser.setTitle(eElement.getAttribute("title").trim());
//            ser.setFrequency(eElement.getAttribute("frequency").trim());
//            ser.setUnits(eElement.getAttribute("units").trim());
//            ser.setType(ResponseType.LIN);
//            ser.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment").trim());
//            ser.setSeasonalAdjustmentShort(eElement.getAttribute("seasonal_adjustment_short").trim());
//            ser.setLastUpdate(eElement.getAttribute("last_updated").trim());
//            ser.setFirstObservation(eElement.getAttribute("observation_start").trim());
//            ser.setLastObservation(eElement.getAttribute("observation_end").trim());
//            ser.setFileDate(null);
//            if (ser.title.length() > 0) {
//              ser.setFullfilename(FredUtils.toFullFileName(ser.id, ser.title));
//            }
//            ser.releaseId = release_id;
//            ser.notes = eElement.getAttribute("notes").trim();
//            ser.valid = true;
//
//            serList.add(ser);
//          }
//        }
//      }
//    }
//    catch (final Exception e) {
//      e.printStackTrace();
//    }
//
//    return serList;
//  }

  private String url;

  private String   id;
  private String   title;
  private DateTime fileDate;
  private String   frequency;
  private DateTime firstObservation;
  private DateTime lastObservation;
  private DateTime lastUpdate;
  private String   fullfilename;
  private String   seasonalAdjustment;
  private String   seasonalAdjustmentShort;
  private String   units;
  private String   notes;
  private String   releaseId;
  private boolean  valid;

  private DataSeries.ResponseType type;

  public Series() {
    this.valid = false;
  }

  public DateTime getFileDate() {
    return this.fileDate;
  }

  public DateTime getFirstObservation() {
    return this.firstObservation;
  }

  public String getFrequency() {
    return this.frequency;
  }

  public String getFullfilename() {
    return this.fullfilename;
  }

  public String getId() {
    return this.id;
  }

  public DateTime getLastObservation() {
    return this.lastObservation;
  }

  public DateTime getLastUpdate() {
    return this.lastUpdate;
  }

  public String getNotes() {
    return this.notes;
  }

  public String getReleaseName() {
    return this.releaseId;
  }

  public String getSeasonalAdjustment() {
    return this.seasonalAdjustment;
  }

  public String getSeasonalAdjustmentShort() {
    return this.seasonalAdjustmentShort;
  }

  public String getTitle() {
    return this.title;
  }

  public DataSeries.ResponseType getType() {
    return this.type;
  }

  public String getUnits() {
    return this.units;
  }

  public String getUrl() {
    return this.url;
  }

  public boolean isValid() {
    return this.valid;
  }

  public void setFileDate(String fredLib) {
    final String fname = String.format("%s/%s.csv", fredLib, this.getId());
    final File f = new File(fname);
    if (f.exists()) {
      this.fileDate = new DateTime(f.lastModified());
    }
    else {
      this.fileDate = null;
    }
  }

  public String toSmallString() {

    String ret = Utils.NL;
    ret += "Id                 : " + this.id + Utils.NL;
    ret += " Title             : " + this.title + Utils.NL;
    ret += " Frequency         : " + this.frequency + Utils.NL;
    ret += " Units             : " + this.units + Utils.NL;
    ret += " Adjustment        : " + this.seasonalAdjustment + Utils.NL;
    ret += " Adjustment S      : " + this.seasonalAdjustmentShort + Utils.NL;
    ret += " Type              : " + this.type + Utils.NL;
    ret += " Full filename     : " + this.fullfilename + Utils.NL;
    if (this.lastUpdate != null) {
      ret += " Last Update       : " + this.lastUpdate + Utils.NL;
    }
    if (this.firstObservation != null) {
      ret += " First Observation : " + this.firstObservation + Utils.NL;
    }
    if (this.lastObservation != null) {
      ret += " Last Observation  : " + this.lastObservation + Utils.NL;
    }
    if (this.fileDate != null) {
      ret += " File Date         : " + this.fileDate.toFullString();
    }
    else {
      ret += " File Date         : " + "File not found.";
    }
    return ret;
  }

  /**
   * <series
   *
   * id="BOMTVLM133S"
   *
   * realtime_start="2017-08-01" realtime_end="2017-08-01"
   *
   * title="U.S. Imports of Services - Travel"
   *
   * observation_start="1992-01-01" observation_end="2017-05-01"
   *
   * frequency="Monthly" frequency_short="M"
   *
   * units="Million of Dollars" units_short="Mil. of $"
   *
   * seasonal_adjustment="Seasonally Adjusted" seasonal_adjustment_short="SA"
   *
   * last_updated="2017-07-06 09:34:00-05"
   *
   * popularity="0" group_popularity="0"/>
   *
   */

  @Override
  public String toString() {

    String ret = Utils.NL;
    ret += "Id                : " + this.id + Utils.NL;
    ret += " Title             : " + this.title + Utils.NL;
    ret += " Frequency         : " + this.frequency + Utils.NL;
    ret += " Units             : " + this.units + Utils.NL;
    ret += " Adjustment        : " + this.seasonalAdjustment + Utils.NL;
    ret += " Adjustment S      : " + this.seasonalAdjustmentShort + Utils.NL;
    ret += " Type              : " + this.type + Utils.NL;
    ret += " Full filename     : " + this.fullfilename + Utils.NL;
    ret += " Notes             : " + this.notes + Utils.NL;
    if (this.lastUpdate != null) {
      ret += " Last Update       : " + this.lastUpdate + Utils.NL;
    }
    if (this.firstObservation != null) {
      ret += " First Observation : " + this.firstObservation + Utils.NL;
    }
    if (this.lastObservation != null) {
      ret += " Last Observation  : " + this.lastObservation + Utils.NL;
    }
    if (this.fileDate != null) {
      ret += " File Date         : " + this.fileDate.toFullString();
    }
    else {
      ret += " File Date         : " + "File not found.";
    }
    ret += Utils.NL;
    ret += this.url + Utils.NL;
    return ret;
  }

  void setFirstObservation(String dateTimeStr) {
    Date d;
    try {
      d = Series.sdf.parse(dateTimeStr);
      this.firstObservation = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  void setFrequency(String frequency) {
    this.frequency = frequency;
  }

  void setFullfilename(String fullfilename) {
    this.fullfilename = fullfilename;
  }

  void setId(String id) {
    this.id = id;
  }

  void setLastUpdate(String dateTimeStr) {
    Date d;
    try {
      d = Series.sdf.parse(dateTimeStr);
      this.lastUpdate = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  void setNotes(String note) {
    this.notes = note;
  }

  void setSeasonalAdjustment(String seasonalAdjustment) {
    this.seasonalAdjustment = seasonalAdjustment;
  }

  void setSeasonalAdjustmentShort(String seasonalAdjustmentShort) {
    this.seasonalAdjustmentShort = seasonalAdjustmentShort;
  }

  void setTitle(String title) {
    this.title = title;
  }

  void setType(DataSeries.ResponseType type) {
    this.type = type;
  }

  void setUnits(String units) {
    this.units = units;
  }

  void setUrl(String url) {
    this.url = url;
  }

  void setValid(boolean valid) {
    this.valid = valid;
  }

  private void setLastObservation(String dateTimeStr) {
    Date d;
    try {
      d = Series.sdf.parse(dateTimeStr);
      this.lastObservation = new DateTime(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

}
