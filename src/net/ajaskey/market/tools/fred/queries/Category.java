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

public class Category {

  private String id;
  private String name;
  private String parent_id;

  private static List<Category> catList = new ArrayList<>();

  private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  private static DocumentBuilder              dBuilder  = null;

  public static void main(String[] args) {

    ApiKey.set();

    // String url =
    // "https://api.stlouisfed.org/fred/category?category_id=125&api_key=&api_key="
    // + ApiKey.get();

    try {

      for (int i = 0; i < 50000; i++) {

        String url = String.format("https://api.stlouisfed.org/fred/category?category_id=%d&api_key=&api_key=%s", i, ApiKey.get());
        // System.out.println(url);

        final String resp = Utils.getFromUrl(url);

        if (resp.length() > 0) {

          // Debug.LOGGER.info(resp + Utils.NL);

          if (dBuilder == null) {
            dBuilder = dbFactory.newDocumentBuilder();
          }

          final Document doc = dBuilder.parse(new InputSource(new StringReader(resp)));

          doc.getDocumentElement().normalize();

          final NodeList nResp = doc.getElementsByTagName("category");

          for (int knt = 0; knt < nResp.getLength(); knt++) {

            final Node nodeResp = nResp.item(knt);

            if (nodeResp.getNodeType() == Node.ELEMENT_NODE) {

              final Element eElement = (Element) nodeResp;

              Category cat = new Category();
              cat.id = eElement.getAttribute("id");
              cat.name = eElement.getAttribute("name");
              cat.parent_id = eElement.getAttribute("parent_id");

              catList.add(cat);
            }
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    for (Category cat : catList) {
      System.out.println(cat);
    }

  }

  @Override
  public String toString() {
    String ret = String.format("id=%5s    name=%s   parent_id=%s", id, name, parent_id);
    return ret;
  }

  public List<Category> getCatList() {
    return catList;
  }

}
