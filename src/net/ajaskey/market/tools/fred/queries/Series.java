package net.ajaskey.market.tools.fred.queries;

import java.io.File;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeries.ResponseType;
import net.ajaskey.market.tools.fred.FredUtils;

public class Series {

  public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

  private static DocumentBuilder dBuilder = null;

  /**
   *
   * @param series_id
   * @return
   */
  public static Series querySeries(String series_id) {

    final Series ser = new Series();

    try {

      ser.setId(series_id.trim());

      final String url = "https://api.stlouisfed.org/fred/series?series_id=" + ser.id + "&api_key=" + ApiKey.get();
      ser.setUrl(url);

      String resp;

      if (Series.dBuilder == null) {
        Series.dBuilder = Series.dbFactory.newDocumentBuilder();
      }

      resp = Utils.getFromUrl(url);

      if (resp.length() < 1) {
        return ser;
      }

      ser.setResponse(resp.trim());
      // Debug.LOGGER.info(this.response + Utils.NL);

      boolean found = false;
      if (resp.length() > 0) {

        final Document doc = Series.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");
        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {
            final Element eElement = (Element) nodeResp;

            ser.setResponse(resp.trim());
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
              found = true;
              ser.setFullfilename(FredUtils.toFullFileName(ser.id, ser.title));
            }
            ser.notes = eElement.getAttribute("notes").trim();
          }
        }
        ser.valid = found;
      }
    }
    catch (final Exception e) {
      ser.valid = false;
      e.printStackTrace();
    }
    return ser;

  }

  /**
   *
   * @param release_id
   * @return
   */
  public static List<Series> querySeriesPerRelease(String release_id) {

    ApiKey.set();

    final List<Series> serList = new ArrayList<>();

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/release/series?release_id=%s&api_key=%s", release_id, ApiKey.get());

      final String resp = Utils.getFromUrl(url);

      if (resp.length() > 0) {

        // Debug.LOGGER.info(resp + Utils.NL);

        if (Series.dBuilder == null) {
          Series.dBuilder = Series.dbFactory.newDocumentBuilder();
        }

        final Document doc = Series.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            final Series ser = new Series();

            ser.setResponse(resp.trim());
            ser.setUrl(url);
            ser.setId(eElement.getAttribute("series_id").trim());
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
            ser.releaseId = release_id;
            ser.notes = eElement.getAttribute("notes").trim();
            ser.valid = true;

            serList.add(ser);
          }
        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    return serList;
  }

  private String id;

  private String                  title;
  private String                  url;
  private String                  response;
  private DateTime                fileDate;
  private String                  frequency;
  private DateTime                firstObservation;
  private DateTime                lastObservation;
  private DateTime                lastUpdate;
  private String                  fullfilename;
  private String                  seasonalAdjustment;
  private String                  seasonalAdjustmentShort;
  private DataSeries.ResponseType type;
  private String                  units;
  private String                  notes;
  private String                  releaseId;
  private boolean                 valid;

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

  public String getResponse() {
    return this.response;
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

    String ret = "";
    ret += "Id                : " + this.id + Utils.NL;
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

    String ret = Utils.NL + this.response.trim() + Utils.NL;
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
    ret += this.response;
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

  void setResponse(String response) {
    this.response = response;
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
