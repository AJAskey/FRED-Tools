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

    Series ser1 = Series.query("CMWRPOP", 10, 2);
    System.out.println(ser1);

    Series ser2 = Series.query("GDP", 10, 2);
    System.out.println(ser2);

    // JTU7100QUL
    Series ser3 = Series.query("JTU7100QUL", 10, 2);
    System.out.println(ser3);
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

      knt++;
      Debug.LOGGER.info(knt + "  " + s);
      dbg += String.format("%-30s %-4s %-20s %-10s%n", s.getId(), s.getSeasonalAdjustmentShort(), s.getFrequency(), s.getTitle());
    }
    Debug.LOGGER.info(dbg);
  }

}
