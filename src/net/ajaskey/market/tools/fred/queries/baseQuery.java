package net.ajaskey.market.tools.fred.queries;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

public class baseQuery {

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  public static List<String> query(String url, String apikey, String params, boolean doOffset, String tagName) {

    // final Set<String> uniqCodes = new HashSet<>();

    String queryUrl = String.format("%s?api_key=%s%s", url, apikey, params);
    String resp = Utils.getFromUrl(queryUrl);
    System.out.println(queryUrl);
    System.out.println(resp);

    if (dBuilder == null) {
      try {

        dBuilder = dbFactory.newDocumentBuilder();

        final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));
        doc.getDocumentElement().normalize();

        final NodeList nResp = doc.getElementsByTagName(tagName);
        for (int knt = 0; knt < nResp.getLength(); knt++) {

          final Node nodeResp = nResp.item(knt);

          if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

            final Element eElement = (Element) nodeResp;

            Release rel = new Release();
//            rel.id = eElement.getAttribute("id");

            // relList.add(rel);
          }
        }

      }
      catch (ParserConfigurationException | SAXException | IOException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  private static String queryResponse(String url) {
    String resp = Utils.getFromUrl(url);
    return resp;
  }

  public static void main(String[] args) {
    ApiKey.set();
    List<String> r = query("https://api.stlouisfed.org/fred/releases", ApiKey.get(), "", true, "release");

  }

}
