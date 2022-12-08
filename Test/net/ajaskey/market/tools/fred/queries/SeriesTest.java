package net.ajaskey.market.tools.fred.queries;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;

class SeriesTest {

  @Test
  void testQuerySeries() {
    Debug.init("debug/testQuerySeries.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

//    final Series ser = Series.querySeries("CAPUTLG325212S", 0);
//    ser.setFileDate("D:\\github\\FRED-Git\\FRED-Tools\\FredLib");
//
//    System.out.println(ser);
  }

  @Test
  void testQuerySeriesPerRelease() {
    Debug.init("debug/testQuerySeriesPerRelease.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    final List<Series> serList = Series.querySeriesPerRelease("53", 2, 15);

    System.out.println("Series knt : " + serList.size());

    int knt = 0;
    String dbg = "All Seried Ids returned : " + Utils.NL;
    for (Series s : serList) {

      s.setFileDate("D:\\github\\FRED-Git\\FRED-Tools\\FredLib");

      knt++;
      Debug.LOGGER.info(knt + "  " + s);
      dbg += String.format("%-30s %-4s %-20s %-10s%n", s.getId(), s.getSeasonalAdjustmentShort(), s.getFrequency(), s.getTitle());
    }
    Debug.LOGGER.info(dbg);
  }

}
