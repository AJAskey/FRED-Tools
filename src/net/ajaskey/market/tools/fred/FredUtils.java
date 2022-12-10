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
import net.ajaskey.common.Utils;

public class FredUtils {

  public final static String fredPath = ".";

  private static String dataSeriesInfoFile = "";
  private static String library            = "data";

  final public static String infoHeader    = "Name\tTitle\tMethod\tFrequency\tUnits\tType\tLast Update\tLast Data Observation\tFirst Data Observation\tFull Filename";
  final public static String infoHeaderCsv = "Name,Title,Method,Frequency,Units,Type,Last Update,Last Data Observation,First Data Observation";

  final public static double BILLION  = 1E9;
  final public static double MILLION  = 1E6;
  final public static double THOUSAND = 1E3;

  public static String getDataSeriesInfoFile() {
    return FredUtils.dataSeriesInfoFile;
  }

  /**
   *
   * @param dir
   * @return
   */
  public static List<String> getFilenamesFromDir(File dir) {

    final Set<String> uniqCodes = new HashSet<>();

    final File[] existingFiles = dir.listFiles();
    for (final File f : existingFiles) {
      final String name = f.getName();
      if (!name.startsWith("[")) {
        uniqCodes.add(name.replaceAll(".csv", ""));
      }
    }
    final List<String> codes = new ArrayList<>(uniqCodes);
    Collections.sort(codes);

    return codes;
  }

  public static String getLibrary() {
    return FredUtils.library;
  }

  /**
   * This scaler is use to change FRED data of various sizes to equal units. Not
   * always needed but can help when comparing values in charting tool or merging
   * various data series.
   *
   * @param unt
   * @return
   */
  public static double getScaler(final String unt) {

    final String units = unt.trim().toLowerCase();
    double ret = 1.0;

    if (units.contains("billion")) {
      ret = FredUtils.BILLION;
    }
    else if (units.contains("million")) {
      ret = FredUtils.MILLION;
    }
    else if (units.contains("thousand")) {
      ret = FredUtils.THOUSAND;
    }

    return ret;
  }

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

  public static String replace(final String in, final String fnd, final String rep) {

    final String ret = in.replaceAll(fnd, rep).replaceAll(fnd.toUpperCase(), rep).replaceAll(fnd.toLowerCase(), rep).trim();
    return ret;
  }

  /**
   * Sets the <b>fred-series-info</b> file. Writes warning to debug if file does
   * not exist.
   *
   * @param fname File to use as <b>fred-series-info</b> file
   */
  public static void setDataSeriesInfoFile(String fname) {
    FredUtils.dataSeriesInfoFile = fname;
    final File f = new File(fname);
    if (!f.exists()) {
      Debug.LOGGER.info(String.format("Warning : FRED DataSeriesInfoFile does not exist : %s", FredUtils.dataSeriesInfoFile));
    }
    Debug.LOGGER.info(String.format("FRED DataSeriesInfoFile set : %s", FredUtils.dataSeriesInfoFile));
  }

  /**
   * Set directory to read/write series date/value pairs data from FRED.
   *
   * @param flib Directory name. If directory does not exist then it is created.
   */
  public static void setLibrary(String flib) {
    FredUtils.library = flib;
    final File f = new File(flib);
    if (!f.isDirectory()) {
      Utils.makeDir(flib);
      Debug.LOGGER.info(String.format("Warning : FRED Libary directory does not exist. Creating : %s", FredUtils.library));
    }
    Debug.LOGGER.info(String.format("FRED Library set : %s", FredUtils.library));

  }

  /**
   * Creates file with the title as the name. Can be useful or can be a pain to
   * work with. You make the call.
   *
   * @param series Short name or FRED Series Id
   * @param title  Long title retrieved from FRED
   * @return
   */
  public static String toFullFileName(final String series, final String title) {

    final int ffnmaxlen = 120;
    final String titl = FredUtils.cleanTitle(title);
    String ret = "[" + series + "] - " + titl;

    final int len = ret.length();
    if (len > ffnmaxlen) {
      ret = ret.substring(0, ffnmaxlen).trim();
      Debug.LOGGER.info(String.format("Warning : Full filename too long (%d). Truncated to %d characters.", len, ffnmaxlen));
    }
    return ret;
  }

  /**
   *
   * @param dsi
   * @param pw
   * @throws FileNotFoundException
   */
  public static void writeSeriesInfo(final List<DataSeriesInfo> dsiList, final String filename) throws FileNotFoundException {

    final DateTime dt = new DateTime();

    File f = new File(filename);
    if (f.exists()) {
      f.delete();
    }

    try (PrintWriter pw = new PrintWriter(filename)) {
      pw.printf("%s\t%s%n", FredUtils.infoHeader, dt.toFullString());
      for (final DataSeriesInfo dsi : dsiList) {

        if (dsi != null) {
          pw.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n", dsi.getName(), dsi.getTitle(), dsi.getSeasonalAdjusted(), dsi.getFrequency(),
              dsi.getUnits(), dsi.getType().toString(), dsi.getLastUpdate(), dsi.getLastObservation(), dsi.getFirstObservation(),
              dsi.getFullfilename());
        }
      }
    }
  }

  /**
   *
   * @param dsiList
   * @param filename
   * @throws FileNotFoundException
   */
  public static void writeSeriesInfoCsv(final List<DataSeriesInfo> dsiList, final String filename) throws FileNotFoundException {

    final DateTime dt = new DateTime();

    try (PrintWriter pw = new PrintWriter(filename)) {
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
   * Write the data retrieved from FRED into file pairs per code. One file has the
   * code as the file name. The other file has a longer description of what is in
   * the file within '[]'.
   *
   * @param fil
   */
  public static void writeToLib(DataSeriesInfo dsi, DataSeries ds, String dir) {

    String ffn = "None";

    if ((dsi != null) && (ds != null)) {
      if (!dsi.isValid() || !ds.isValid()) {
        Debug.LOGGER.info(String.format("Warning. Valid FIL incomplete. Data null %n%s%n%s", dsi, ds));
        return;
      }
      else if ((dsi == null) || (ds == null)) {
        Debug.LOGGER.info(String.format("Warning. FIL incomplete. Data null."));
        return;
      }
    }

    final double scaler = FredUtils.getScaler(dsi.getUnits());

    final String fullFileName = FredUtils.toFullFileName(dsi.getName(), dsi.getTitle());

    ffn = dir + "/" + fullFileName.replace(">", "greater") + ".csv";
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
      ffn = "Error";
      e.printStackTrace();
    }
    dsi.setLastUpdate(new DateTime(file.lastModified()));
  }

  /**
   * Local function to replace words from FRED series title with much shorter
   * versions. Used to make a title smaller for use as the output file name.
   *
   * @param title Original string to update
   * @return Updated string
   */
  private static String cleanTitle(final String title) {

    String sn = title.trim();

    sn = sn.replaceAll("[/\\)\\(:,;\"]", " ");
    sn = sn.replaceAll("U.S.", "US");
    sn = FredUtils.replace(sn, "-Year", "Y");
    sn = FredUtils.replace(sn, "-Month", "M");
    sn = FredUtils.replace(sn, " -", "");

    sn = FredUtils.replace(sn, "Control", "Ctrl");
    sn = FredUtils.replace(sn, "Value of Manufacturers'", "Value");
    sn = FredUtils.replace(sn, "Components", "Comp");
    sn = FredUtils.replace(sn, "Ventilation  Heating  Air-Conditioning", "HVAC");
    sn = FredUtils.replace(sn, "Contributions to percent change in GDPNow", "");
    sn = FredUtils.replace(sn, "Except Manufacturers' Sales Branches and Offices Sales", "");
    sn = FredUtils.replace(sn, "Commercial Paper", "CP");
    sn = FredUtils.replace(sn, "Durable Goods", "DG");
    sn = FredUtils.replace(sn, "Nondurable Goods", "NDG");
    sn = FredUtils.replace(sn, "United States", "US");
    sn = FredUtils.replace(sn, "Real Change of", "RChg");
    sn = FredUtils.replace(sn, "Federal Funds Rate", "FFR");
    sn = FredUtils.replace(sn, "Personal Consumption Expenditures", "PCE");
    sn = FredUtils.replace(sn, "Nonfinancial", "NonFin");
    sn = FredUtils.replace(sn, "Government", "Govt");
    sn = FredUtils.replace(sn, "London Interbank Offered Rate", "");
    sn = FredUtils.replace(sn, "Owned and Securitized", "OwnedSecured");
    sn = FredUtils.replace(sn, "Private Domestic", "Priv Dom");
    sn = FredUtils.replace(sn, "Capacity Utilization", "CapUtil");
    sn = FredUtils.replace(sn, "Transportation", "Transport");
    sn = FredUtils.replace(sn, "Durable Manufacturing", "Dur Manufacturing");
    sn = FredUtils.replace(sn, "Nondurable Manufacturing", "NonDur Manufacturing");
    sn = FredUtils.replace(sn, "miscellaneous", "Misc");
    sn = FredUtils.replace(sn, "Equipment", "Equip");
    sn = FredUtils.replace(sn, "Corporate", "Corp");
    sn = FredUtils.replace(sn, "Information", "Info");
    sn = FredUtils.replace(sn, "Organizations", "Orgs");
    sn = FredUtils.replace(sn, "Diffusion", "Diff");
    sn = FredUtils.replace(sn, "Investment", "Invest");
    sn = FredUtils.replace(sn, "Capital Goods", "CapGoods");
    sn = FredUtils.replace(sn, "development", "Devel");
    sn = FredUtils.replace(sn, "Consumer Price Index", "CPI");
    sn = FredUtils.replace(sn, "Producer Price Index", "PPI");
    sn = FredUtils.replace(sn, "Industries", "Ind");
    sn = FredUtils.replace(sn, "Nondefense", "NonDef");
    sn = FredUtils.replace(sn, "Federal Debt", "Fed Debt");
    sn = FredUtils.replace(sn, "Federal Reserve", "Fed Reserve");
    sn = FredUtils.replace(sn, "Gross Domestic Product", "GDP");

    sn = FredUtils.toSentenceCase(sn);

    Debug.LOGGER.fine(String.format("Modified Title%nInput= %s%nMod  = %s", title, sn));

    return sn;
  }

  /**
   * Local function to determine words to ignore when creating a full file name.
   *
   * @param word String to check
   * @return True if word is to be ignored.
   */
  private static boolean isArticle(final String word) {

    if (word.equalsIgnoreCase("the")) {
      return true;
    }
    else if (word.equalsIgnoreCase("and")) {
      return true;
    }
    else if (word.equalsIgnoreCase("except")) {
      return true;
    }
    else if (word.equalsIgnoreCase("for")) {
      return true;
    }
    else if (word.equalsIgnoreCase("of")) {
      return true;
    }
    else if (word.equalsIgnoreCase("with")) {
      return true;
    }
    else if (word.equalsIgnoreCase("to")) {
      return true;
    }
    else if (word.equalsIgnoreCase("on")) {
      return true;
    }
    return false;
  }

  /**
   * Local function to remove small non-descriptive words and capitalize the first
   * letter of each word.
   *
   * @param title
   * @return
   */
  private static String toSentenceCase(final String title) {

    String str = title.trim();

    final StringBuilder sb = new StringBuilder();

    final String fld[] = str.split("\\s+");

    for (final String s : fld) {
      if (!FredUtils.isArticle(s)) {
        sb.append(s.substring(0, 1).toUpperCase() + s.substring(1));
        sb.append(" ");
      }
      else {
        sb.append(s + " ");
      }
    }

    final String ret = sb.toString().replaceAll("\\s+", " ").trim();

    return ret;
  }

}
