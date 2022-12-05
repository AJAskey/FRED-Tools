package net.ajaskey.market.tools.fred.executables;

import java.util.Collections;
import java.util.List;

import net.ajaskey.common.Debug;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.DsiAbcSorter;

public class FredSeriesUpdates {

  public static void main(String[] args) {

    Debug.init("debug/FredSeriesUpdates.dbg", java.util.logging.Level.INFO);

    ApiKey.set();

    List<DataSeriesInfo> list = DataSeriesInfo.getDataSeriesNames();

    Collections.sort(list, new DsiAbcSorter());
    for (DataSeriesInfo dsi : list) {
      Debug.LOGGER.info(String.format("%n%s", dsi));
      System.out.printf("%-12s : %s%n", dsi.getName(), dsi.getTitle());

    }
    Debug.LOGGER.info(String.format("Series for update : %d", list.size()));

  }

}
