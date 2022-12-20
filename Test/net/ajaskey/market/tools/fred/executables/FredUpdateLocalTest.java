package net.ajaskey.market.tools.fred.executables;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.LocalFormat;
import net.ajaskey.market.tools.fred.queries.Series;

class FredUpdateLocalTest {

  @Test
  void testOne() {

    Debug.init("debug/FredUpdateLocal_OneTest.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    Series ser1 = Series.query("A068RC1", 3, 5);

    LocalFormat lf = new LocalFormat(ser1, "999", "reltest", "FredLib");

    Assert.assertNotNull(lf);

    FredUpdateLocal.process(lf, "FredLib", 5, 8);

    System.out.println(lf);
  }

  @Test
  void testTwo() {

    Debug.init("debug/FredUpdateLocal_TwoTest.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    final List<LocalFormat> lfList = LocalFormat.readSeriesList("out/filteredSeriesSummary.txt", "FredLib");

    LocalFormat lf = LocalFormat.findInList("DPRIME", lfList);

    Assert.assertNotNull(lf);

    FredUpdateLocal.process(lf, "FredLib", 5, 8);

    System.out.println(lf);
  }

  @Test
  void testBigRead() {

    Debug.init("debug/FredUpdateLocal_BigReadTest.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    Debug.LOGGER.info("Start...");

    List<LocalFormat> lfList = LocalFormat.readReleaseSeriesInfoDir("FredSeries", "FredLib");

    Assert.assertNotNull(lfList);
    Assert.assertNotEquals(lfList.size(), 0);

    Debug.LOGGER.info(String.format("...Stop  size=%d", lfList.size()));

    LocalFormat lf = LocalFormat.findInList("A068RC1", lfList);

    Assert.assertNotNull(lf);

    FredUpdateLocal.process(lf, "FredLib", 5, 8);

    Debug.LOGGER.info("Done.");
  }

}
