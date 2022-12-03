package net.ajaskey.market.tools.fred;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Utils;

public class DsiQuery {

  /**
   *
   * @param dir
   * @return
   */
  public static List<DataSeriesInfo> queryDataSeriesInfo(File dir) {

    final List<DsiQuery> dsiqList = new ArrayList<>();

    try {

      final List<String> filenameList = FredUtils.getFilenamesFromDir(dir);

      for (final String name : filenameList) {
        final DsiQuery dsiq = new DsiQuery(name);
        dsiqList.add(dsiq);
      }

      int moreToDo = 1;
      int lastMoreToDo = 0;
      while (moreToDo > 0) {
        moreToDo = DsiQuery.process(dsiqList);
        if (moreToDo == lastMoreToDo) {
          break;
        }
        lastMoreToDo = moreToDo;
        Utils.sleep(10000);
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    final List<DataSeriesInfo> retList = new ArrayList<>();
    for (final DsiQuery dsiq : dsiqList) {
      if (dsiq.dsi != null) {
        retList.add(dsiq.dsi);
      }
    }

    return retList;

  }

  /**
   *
   * @param filename
   * @return
   */
  public static List<DataSeriesInfo> queryDataSeriesInfo(String filename) {

    final List<DsiQuery> dsiqList = new ArrayList<>();

    try {
      final List<String> nameList = FredUtils.readSeriesList(filename);
      for (final String name : nameList) {
        final DsiQuery dsiq = new DsiQuery(name);
        dsiqList.add(dsiq);
      }

      int moreToDo = 1;
      int lastMoreToDo = 0;
      while (moreToDo > 0) {
        moreToDo = DsiQuery.process(dsiqList);
        if (moreToDo == lastMoreToDo) {
          break;
        }
        lastMoreToDo = moreToDo;
        Utils.sleep(10000);
      }
    }
    catch (final Exception e) {
      e.printStackTrace();
    }

    final List<DataSeriesInfo> retList = new ArrayList<>();
    for (final DsiQuery dsiq : dsiqList) {
      if (dsiq.dsi != null) {
        retList.add(dsiq.dsi);
      }
    }

    return retList;
  }

  /**
   *
   * @param list
   * @return
   */
  static int process(List<DsiQuery> list) {

    int unprocessed = 0;
    final DateTime dt = new DateTime();

    for (final DsiQuery dsiq : list) {

      if (dsiq.dsi == null) {
        final DataSeriesInfo dsi = new DataSeriesInfo(dsiq.name, dt);
        if (dsi.isValid()) {
          dsiq.dsi = dsi;
        }
        else {
          unprocessed++;
        }
      }
    }
    return unprocessed;
  }

  private final String   name;
  private DataSeriesInfo dsi;

  public DsiQuery(String n) {
    this.name = n;
    this.dsi = null;
  }

}
