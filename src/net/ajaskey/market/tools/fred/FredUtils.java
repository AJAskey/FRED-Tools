package net.ajaskey.market.tools.fred;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.TextUtils;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.queries.Observations;

public class FredUtils {

  public final static String fredPath = ".";

  private static String dataSeriesInfoFile = "";
  private static String library            = "data";

  final public static String infoHeader    = "Name\tTitle\tMethod\tFrequency\tUnits\tType\tLast Update\tLast Data Observation\tFirst Data Observation\tFull Filename";
  final public static String infoHeaderCsv = "Name,Title,Method,Frequency,Units,Type,Last Update,Last Data Observation,First Data Observation";

  final public static double BILLION  = 1E9;
  final public static double MILLION  = 1E6;
  final public static double THOUSAND = 1E3;

  final static public String optumaDateFormat = "yyyy-MM-dd";

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

  /**
   *
   * @param f
   * @return
   */
  public static DateTime getLastObservation(File f) {

    DateTime dt = new DateTime(2000, DateTime.JANUARY, 1);
    try {
      final List<String> data = TextUtils.readTextFile(f, false);
      final String s = data.get(data.size() - 1);
      final String ss[] = s.split(",");
      dt = new DateTime(ss[0].trim(), FredUtils.optumaDateFormat);
    }
    catch (final Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      Debug.LOGGER.info(String.format("Warning : Exception Thrown%n%s", pw.toString()));
    }
    return dt;
  }

  /**
   * 
   * @return Current FRED Library directory
   */
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
   * @param obs
   * @param lf
   * @param dir
   */
  public static void writeToLib(Observations obs, LocalFormat lf, String dir) {

    double scaler = 1.0;
    if (lf.getUnits().equals("B")) {
      scaler = FredUtils.BILLION;
    }
    else if (lf.getUnits().equals("M")) {
      scaler = FredUtils.MILLION;
    }
    else if (lf.getUnits().equals("T")) {
      scaler = FredUtils.THOUSAND;
    }

    final String fullFileName = FredUtils.toFullFileName(obs.getId(), lf.getTitle());

    String ffn = dir + "/" + fullFileName.replace(">", "greater") + ".csv";
    final File file = new File(ffn);
    final File fileshort = new File(dir + "/" + obs.getId() + ".csv");

    Debug.LOGGER.info(String.format("Long File=%s    ShortFile=%s", file.getAbsoluteFile(), fileshort.getAbsoluteFile()));

    // Remove existing file so new file will show date of creation. Must be a
    // Windows feature to keep original file date when it is overwritten with new.
    if (file.exists()) {
      Debug.LOGGER.info("Deleting existing file : " + file.getAbsolutePath());
      file.delete();
    }
    if (fileshort.exists()) {
      Debug.LOGGER.info("Deleting existing file : " + fileshort.getAbsolutePath());
      fileshort.delete();
    }

    try (PrintWriter pw = new PrintWriter(file); PrintWriter pwShort = new PrintWriter(fileshort)) {
      pw.println("Date," + obs.getId());
      pwShort.println("Date," + obs.getId());
      for (final DataValue dv : obs.getDvList()) {
        final String sDate = dv.getDate().format(FredUtils.optumaDateFormat);
        final double d = dv.getValue() * scaler;
        pw.printf("%s,%.2f%n", sDate, d);
        pwShort.printf("%s,%.2f%n", sDate, d);
      }
      Debug.LOGGER.info("Wrote 2 new data files.");
    }
    catch (final FileNotFoundException e) {
      ffn = "Error";
      e.printStackTrace();
      Debug.LOGGER.info(String.format("Exception caught for %s", lf.getId()));
    }
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

    final String str = title.trim();

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
