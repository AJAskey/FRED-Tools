package net.ajaskey.market.tools.fred.queries;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

public class Series {

  private String id;
  private String title;
  private String realtime_start;
  private String realtime_end;
  private String observation_start;
  private String observation_end;
  private String last_updated;
  private String units;
  private String units_short;
  private String frequency;
  private String frequency_short;
  private String seasonal_adjustment;
  private String seasonal_adjustment_short;

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

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  public static void main(String[] args) {
    List<Series> serList = querySeries("10");
    for (Series ser : serList) {
      System.out.println(ser);
    }
  }

  public static List<Series> querySeries(String release_id) {

    ApiKey.set();

    List<Series> serList = new ArrayList<>();

    try {

      String url = String.format("https://api.stlouisfed.org/fred/release/series?release_id=%s&api_key=%s", release_id, ApiKey.get());

      final String resp = Utils.getFromUrl(url);

      if (resp.length() > 0) {

        // Debug.LOGGER.info(resp + Utils.NL);

        if (dBuilder == null) {
          dBuilder = dbFactory.newDocumentBuilder();
        }

        final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("series");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            Series ser = new Series();
            ser.id = eElement.getAttribute("id");
            ser.title = eElement.getAttribute("title");
            ser.realtime_start = eElement.getAttribute("realtime_start");
            ser.realtime_end = eElement.getAttribute("realtime_end");
            ser.realtime_start = eElement.getAttribute("realtime_start");
            ser.realtime_end = eElement.getAttribute("realtime_end");
            ser.observation_start = eElement.getAttribute("observation_start");
            ser.observation_end = eElement.getAttribute("observation_end");
            ser.last_updated = eElement.getAttribute("last_updated");
            ser.frequency = eElement.getAttribute("frequency");
            ser.frequency_short = eElement.getAttribute("frequency_short");
            ser.units = eElement.getAttribute("units");
            ser.units_short = eElement.getAttribute("units_short");
            ser.units_short = eElement.getAttribute("units_short");
            ser.seasonal_adjustment = eElement.getAttribute("seasonal_adjustment");
            ser.seasonal_adjustment_short = eElement.getAttribute("seasonal_adjustment_short");

            serList.add(ser);
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return serList;
  }

  @Override
  public String toString() {
    int len = title.trim().length();
    String t;
    if (len > 160) {
      t = title.substring(0, 159);
    }
    else {
      t = title.trim();
    }
    String ret = String.format("Series Id=%s\tTitle=%s\tFrequency=%s", id, t, frequency);
    return ret;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getRealtime_start() {
    return realtime_start;
  }

  public String getRealtime_end() {
    return realtime_end;
  }

  public String getObservation_start() {
    return observation_start;
  }

  public String getObservation_end() {
    return observation_end;
  }

  public String getLast_updated() {
    return last_updated;
  }

  public String getUnits() {
    return units;
  }

  public String getUnits_short() {
    return units_short;
  }

  public String getFrequency() {
    return frequency;
  }

  public String getFrequency_short() {
    return frequency_short;
  }

  public String getSeasonal_adjustment() {
    return seasonal_adjustment;
  }

  public String getSeasonal_adjustment_short() {
    return seasonal_adjustment_short;
  }

}
