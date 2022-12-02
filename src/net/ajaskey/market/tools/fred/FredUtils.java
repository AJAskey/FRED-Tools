package net.ajaskey.market.tools.fred;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.TextUtils;
import net.ajaskey.market.tools.fred.legacy.FredCommon;

public class FredUtils {

  public final static String fredPath = ".";

  final static String infoHeaderCsv = "Name,Title,Method,Frequency,Units,Type,Last Update,Last Data Observation,First Data Observation";

  /**
   *
   * @param fname
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static List<String> readSeriesList(final String fname) throws FileNotFoundException, IOException {

    final Set<String> uniqCodes = new HashSet<>();

    final List<String> series = TextUtils.readTextFile(fname, true);

    for (final String element : series) {
      final String str = element.trim();
      final String fld[] = str.split("\\s+");
      final String code = fld[0].trim().toUpperCase();
      // Ignore header line of the file
      if (!code.equalsIgnoreCase("NAME")) {
        uniqCodes.add(code);
      }
    }

    final List<String> ret = new ArrayList<>(uniqCodes);
    Collections.sort(ret);
    return ret;
  }

  /**
   * 
   * @param dsiList
   * @param filename
   * @throws FileNotFoundException
   */
  public static void writeSeriesInfoCsv(final List<DataSeriesInfo> dsiList, final String filename) throws FileNotFoundException {

    final File f = new File(filename);
    final DateTime dt = new DateTime();

    try (PrintWriter pw = new PrintWriter(f)) {
      pw.printf("%s,%s%n", FredUtils.infoHeaderCsv, dt.toFullString());
      for (final DataSeriesInfo dsi : dsiList) {

        if (dsi != null) {
          pw.printf("%s,%s,%s,%s,%s,%s,%s,%s, %s%n", dsi.getName(), dsi.getTitle().replaceAll(",", ";"),
              dsi.getSeasonalAdjusted().replaceAll(",", ";"), dsi.getFrequency().replaceAll(",", ";"), dsi.getUnits().replaceAll(",", ";"),
              dsi.getType().toString().replaceAll(",", ";"), dsi.getLastUpdate(), dsi.getLastObservation(), dsi.getFirstObservation());
        }
      }
    }
  }

  /**
   * 
   * @param unt
   * @return
   */
  public static double getScaler(final String unt) {

    final String units = unt.trim().toLowerCase();
    double ret = 1.0;

    if (units.contains("billion")) {
      ret = FredCommon.BILLION;
    }
    else if (units.contains("million")) {
      ret = FredCommon.MILLION;
    }
    else if (units.contains("thousand")) {
      ret = FredCommon.THOUSAND;
    }

    return ret;
  }

  /**
   * 
   * @param series
   * @param title
   * @return
   */
  public static String toFullFileName(String dir, final String series, final String title) {

    final String titl = FredCommon.cleanTitle(title);
    String ret = dir + "/" + "[" + series + "] - " + titl;
    final int len = ret.length();
    if (len > 250) {
      ret = ret.substring(0, 250).trim();
    }
    return ret + ".csv";
  }

  /**
   * Write the data retrieved from FRED into file pairs per code. One file has the
   * code as the file name. The other file has a longer description of what is in
   * the file within '[]'.
   *
   * @param fil
   */
  public static void writeToLib(DataSeriesInfo dsi, DataSeries ds, String dir) {

    if (!dsi.isValid()) {
      return;
    }
    if (!ds.isValid()) {
      return;
    }

    final double scaler = getScaler(dsi.getUnits());

    final String fullFileName = toFullFileName(dir, dsi.getName(), dsi.getTitle());

    final String ffn = fullFileName.replace(">", "greater");
    final File file = new File(ffn);
    final File fileshort = new File(dir + "/" + dsi.getName() + ".csv");

    Debug.LOGGER.info(String.format("Long File=%s    ShortFile=%s", file.getAbsoluteFile(), fileshort.getAbsoluteFile()));

    try (PrintWriter pw = new PrintWriter(file); PrintWriter pwShort = new PrintWriter(fileshort)) {
      pw.println("Date," + dsi.getFileDt());
      pwShort.println("Date," + dsi.getName());
      for (final DataValues dv : ds.getDvList()) {
        final String date = dv.getDate().format("yyyy-MM-dd");
        final double d = dv.getValue() * scaler;
        pw.printf("%s,%.2f%n", date, d);
        pwShort.printf("%s,%.2f%n", date, d);
      }
    }
    catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

}
