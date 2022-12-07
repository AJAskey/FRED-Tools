package net.ajaskey.market.tools.fred.queries;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;

class SeriesTest {

  @Test
  void testQuerySeries() {
    Debug.init("debug/testQuerySeries.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    final Series ser = Series.querySeries("CAPUTLG325212S");
    ser.setFileDate("D:\\github\\FRED-Git\\FRED-Tools\\FredLib");

    System.out.println(ser);
  }

  @Test
  void testQuerySeriesPerRelease() {
    Debug.init("debug/testQuerySeriesPerRelease.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    final List<Series> serList = Series.querySeriesPerRelease("27");

    Debug.LOGGER.info(serList.get(0).getUrl());
    Debug.LOGGER.info(serList.get(0).getResponse());
    for (Series s : serList) {
      s.setFileDate("D:\\github\\FRED-Git\\FRED-Tools\\FredLib");
      Debug.LOGGER.info(s.toSmallString());
    }
  }

}
