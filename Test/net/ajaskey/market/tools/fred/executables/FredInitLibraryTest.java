package net.ajaskey.market.tools.fred.executables;

import org.junit.jupiter.api.Test;

import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.FredUtils;

class FredInitLibraryTest {

  @Test
  void test() {

    Utils.makeDir("Test/debug");
    Utils.makeDir("out");

    Debug.init("Test/debug/FredInitLibraryTest.dbg", java.util.logging.Level.INFO);

    ApiKey.set();
    Utils.makeDir(FredUtils.getLibrary());
    FredUtils.setDataSeriesInfoFile("Test/input/fred-series-info-test.txt");
    FredUtils.setLibrary("Test/data");

    // FredInitLibrary.process();

  }

}
