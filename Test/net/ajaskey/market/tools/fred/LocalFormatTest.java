package net.ajaskey.market.tools.fred;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.TextUtils;
import net.ajaskey.market.tools.fred.queries.Series;

class LocalFormatTest {

  @Test
  void testGenSummary() {

    List<LocalFormat> lfList = LocalFormat.readMasterList("input/MasterSeriesSummary.txt", "D:\\data2\\MA\\CSV Data\\FRED-Download", true);
    List<LocalFormat> lfList2 = new ArrayList<>();

    for (LocalFormat lf : lfList) {
      if (!lf.getFrequency().toUpperCase().contains("DAILY")) {
        lfList2.add(lf);
      }
    }

    Collections.sort(lfList2, LocalFormat.sorter);

    try (PrintWriter pw = new PrintWriter("out/sorted.txt")) {
      for (LocalFormat lf : lfList2) {
        pw.println(lf.formatlineRel());
      }
    }
    catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test
  void testFormatSeries() {

    ApiKey.set();

    Series ser = Series.query("GDP", 3, 5);

    LocalFormat lf = new LocalFormat(ser, "999", "Dummy", "D:\\data2\\MA\\CSV Data\\FRED-Download");

    System.out.println(lf.formatline());
  }

  @Test
  void testReadLocalFormat() {

    ApiKey.set();

    final List<String> data = TextUtils.readTextFile("FredSeries/Id_9AdvanceMonthlySalesforRetailandFoodServices.txt", false);

    for (int i = 1; i < data.size(); i++) {
      String s = data.get(i);
      final LocalFormat lf = new LocalFormat("999\tdummy", "D:\\data2\\MA\\CSV Data\\FRED-Download");
      lf.parseline(s);
      System.out.println(lf.formatline());
    }

  }

}
