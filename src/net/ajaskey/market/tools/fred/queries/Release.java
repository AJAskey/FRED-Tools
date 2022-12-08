package net.ajaskey.market.tools.fred.queries;

import java.io.StringReader;
import java.util.ArrayList;
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

import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

public class Release {

  private static List<Release>                realeaseList = new ArrayList<>();
  private static Set<String>                  uniqReleases = new HashSet<>();
  private final static DocumentBuilderFactory dbFactory    = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder     = null;

  public static void main(String[] args) {
  }

  public static List<Release> queryReleases() {

    Release.realeaseList.clear();

    int offset = 0;
    boolean readmore = true;
    while (readmore) {
      final int num = Release.fredReleaseQuery(offset);
      if (num < 1000) {
        readmore = false;
      }
      else {
        offset += num;
      }
    }

    return Release.realeaseList;
  }

  public Release() {
    this.valid = false;
  }

  public Release(String relId) {
    this.id = relId;
    this.valid = false;
  }

  /**
   *
   * @param offset
   * @return
   */
  private static int fredReleaseQuery(int offset) {

    int totalProcessed = 0;

    try {

      final String url = String.format("https://api.stlouisfed.org/fred/releases?api_key=%s&offset=%d", ApiKey.get(), offset);

      final String resp = Utils.getFromUrl(url);

      if (resp.length() > 0) {

        if (Release.dBuilder == null) {
          Release.dBuilder = Release.dbFactory.newDocumentBuilder();
        }

        final Document doc = Release.dBuilder.parse(new InputSource(new StringReader(resp)));

        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName("release");

        totalProcessed = nResp.getLength();

        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            final Release rel = new Release();
            rel.id = eElement.getAttribute("id");
            rel.name = eElement.getAttribute("name");
            rel.realtime_start = eElement.getAttribute("realtime_start");
            rel.realtime_end = eElement.getAttribute("realtime_end");
            rel.press_release = eElement.getAttribute("press_release");
            rel.link = eElement.getAttribute("link");

            rel.setUrl(url);
            rel.setResponse(resp);

            final boolean newRel = Release.uniqReleases.add(rel.id);
            if (newRel) {
              rel.valid = true;
              Release.realeaseList.add(rel);
            }
            else {
              totalProcessed--;
            }
          }
        }
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    return totalProcessed;
  }

  private String id;
  private String name;
  private String realtime_start;
  private String realtime_end;
  private String press_release;
  private String link;

  private String  url;
  private String  response;
  private boolean valid;

  public String getId() {
    return this.id;
  }

  public String getLink() {
    return this.link;
  }

  public String getName() {
    return this.name;
  }

  public String getPress_release() {
    return this.press_release;
  }

  public String getRealtime_end() {
    return this.realtime_end;
  }

  public String getRealtime_start() {
    return this.realtime_start;
  }

  @Override
  public String toString() {
    final String ret = String.format("Release Id=%s Name=%s    Valid=%s", this.id, this.name, this.valid);
    return ret;
  }

  public boolean isValid() {
    return valid;
  }

  public String getUrl() {
    return url;
  }

  public String getResponse() {
    return response;
  }

  void setUrl(String url) {
    this.url = url;
  }

  void setResponse(String response) {
    this.response = response;
  }

}
