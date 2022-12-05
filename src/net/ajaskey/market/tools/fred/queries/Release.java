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

public class Release {

  private String id;
  private String name;
  private String realtime_start;
  private String realtime_end;
  private String press_release;
  private String link;

  private static List<Release> relList = new ArrayList<>();

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  public static void main(String[] args) {
  }

  public static List<Release> queryReleases() {
    ApiKey.set();

    try {

      String url = String.format("https://api.stlouisfed.org/fred/releases?api_key=&api_key=&api_key=%s", ApiKey.get());

      final String resp = Utils.getFromUrl(url);

      if (resp.length() > 0) {

        // Debug.LOGGER.info(resp + Utils.NL);

        if (dBuilder == null) {
          dBuilder = dbFactory.newDocumentBuilder();
        }

        final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("release");

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            Release rel = new Release();
            rel.id = eElement.getAttribute("id");
            rel.name = eElement.getAttribute("name");
            rel.realtime_start = eElement.getAttribute("realtime_start");
            rel.realtime_end = eElement.getAttribute("realtime_end");
            rel.press_release = eElement.getAttribute("press_release");
            rel.link = eElement.getAttribute("link");

            relList.add(rel);
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return relList;
  }

  @Override
  public String toString() {
    String ret = String.format("Release Id=%s Name=%s", id, name);
    return ret;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getRealtime_start() {
    return realtime_start;
  }

  public String getRealtime_end() {
    return realtime_end;
  }

  public String getPress_release() {
    return press_release;
  }

  public String getLink() {
    return link;
  }

}
