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

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.FredUtils;

public class RecentUpdates {

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  private static List<DataSeriesInfo> bigList = null;

  public static void main(String[] args) {

    bigList = new ArrayList<>();

    Debug.init("debug/RecentUpdates.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    int count = 0;
    boolean moreToGet = true;
    int offset = 0;
    while (moreToGet) {
      List<DataSeriesInfo> dsiList = queryRecentUpdates(offset);
      for (DataSeriesInfo dsi : dsiList) {
        if (isNewValue(dsi)) {
          count++;
          bigList.add(dsi);
          Debug.LOGGER.info("Added : " + dsi.getName() + "  count=" + count + "     bigList.size()=  " + bigList.size());
        }
        else {
          moreToGet = false;
          Debug.LOGGER.info(String.format("bigList size : %d", bigList.size()));
        }
      }
      offset += 1000;
    }

    for (DataSeriesInfo dsi : bigList) {
      System.out.println(dsi);
    }
    System.out.println(bigList.size());
  }

  /**
   * 
   * @param dsi
   * @return
   */
  private static boolean isNewValue(DataSeriesInfo dsi) {
    for (DataSeriesInfo bdsi : bigList) {
      if (dsi.getName().equals(bdsi.getName())) {
        Debug.LOGGER.info(String.format("Repeat Ids %s : %s", dsi.getName(), bdsi.getName()));
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param offset
   * @return
   */
  public static List<DataSeriesInfo> queryRecentUpdates(int offset) {

    final List<DataSeriesInfo> retList = new ArrayList<>();

    String url = String.format("https://api.stlouisfed.org/fred/series/updates?api_key=%s&filter_value=macro&offset=%d", ApiKey.get(), offset);

    try {
      if (dBuilder == null) {
        dBuilder = dbFactory.newDocumentBuilder();
      }

      final String resp = Utils.getFromUrl(url);

      final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));

      doc.getDocumentElement().normalize();

      final NodeList nResp = doc.getElementsByTagName("series");

      for (int knt = 0; knt < nResp.getLength(); knt++) {

        final Node nodeResp = nResp.item(knt);

        if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

          final DataSeriesInfo dsi = new DataSeriesInfo();

          final Element eElement = (Element) nodeResp;

          final String series = eElement.getAttribute("id");
          Debug.LOGGER.info("Series : " + series);

          dsi.setName(series.trim());
          dsi.setTitle(eElement.getAttribute("title").trim());
          dsi.setFrequency(eElement.getAttribute("frequency").trim());
          dsi.setSeasonalAdjustment(eElement.getAttribute("seasonal_adjustment_short").trim());
          dsi.setUnits(eElement.getAttribute("units").trim());
          dsi.setType("LIN");
          dsi.setFirstObservation(eElement.getAttribute("observation_start").trim());
          dsi.setLastObservation(eElement.getAttribute("observation_end").trim());
          dsi.setLastUpdate(eElement.getAttribute("last_updated").trim());
          if (dsi.getTitle().length() > 0) {
            dsi.setFullFilename(FredUtils.toFullFileName(dsi.getName(), dsi.getTitle()));
          }

          dsi.setResponse("Many series updated.");
          dsi.setFileDt(null);

          retList.add(dsi);
        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    return retList;

  }

}
