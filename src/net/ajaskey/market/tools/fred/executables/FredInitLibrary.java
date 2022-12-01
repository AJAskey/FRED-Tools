package net.ajaskey.market.tools.fred.executables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.common.Debug;
import net.ajaskey.common.Utils;
import net.ajaskey.market.tools.fred.ApiKey;
import net.ajaskey.market.tools.fred.DataSeries;
import net.ajaskey.market.tools.fred.DataSeriesInfo;
import net.ajaskey.market.tools.fred.DataValues;
import net.ajaskey.market.tools.fred.FredUtils;

public class FredInitLibrary {

  public static int goodTotal = 0;

  public static List<FredInitLibrary> filList = new ArrayList<>();
  public static List<DataSeriesInfo>  dsiList = new ArrayList<>();

  public final static int midPause  = 10;
  final static int        longPause = 15;

  final static String ftLibDir  = FredUtils.fredPath + "/data";
  final static String ftDataDir = FredUtils.fredPath + "/data";

  /**
   * For testing, point to a file with a smaller number of codes
   */
  final static String fsiFilename = ftDataDir + "/fred-series-info.txt";

  public static List<FredInitLibrary> getFilList() {
    return FredInitLibrary.filList;
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {

    ApiKey.set();

    Utils.makeDir(ftLibDir);
    Utils.makeDir("debug");
    Utils.makeDir("out");

    Debug.init("debug/FredInitLibrary.dbg");

    Debug.LOGGER.info(String.format("DataDir=%s  LibDir=%s  fsiFilename=%s", ftDataDir, ftLibDir, fsiFilename));

    final List<String> codeNames = FredUtils.readSeriesList(fsiFilename);

    String codes = "Processing codes :" + Utils.NL;
    for (final String code : codeNames) {

      final FredInitLibrary fil = new FredInitLibrary(code);
      FredInitLibrary.filList.add(fil);
      codes += code + Utils.NL;
    }
    Debug.LOGGER.info(codes);

    final DateTime dt = new DateTime(2000, DateTime.JANUARY, 1);

    int moreToDo = 1;
    int lastMoreToDo = 0;
    while (moreToDo > 0) {
      moreToDo = FredInitLibrary.process(dt);
      Debug.LOGGER.info(String.format("%n--------------------------------%n%nReturn from Processing with moreToDo=%d.", moreToDo));
      if (moreToDo > 0) {
        // Case where all codes are junk and will never be found at FRED.
        if (lastMoreToDo >= moreToDo) {
          Debug.LOGGER.info(String.format("Finished, only junk code(s) remain to be found.%n---------------------------------%n%n"));
          break;
        }
        lastMoreToDo = moreToDo;
        Debug.LOGGER.info(String.format("%nPausing %d seconds.%n---------------------------------%n%n", FredInitLibrary.midPause));
        Utils.sleep(FredInitLibrary.midPause * 1000);
      }
    }

    for (final FredInitLibrary fil : FredInitLibrary.filList) {
      System.out.println(fil.getName());
      FredInitLibrary.writeToLib(fil);
      FredInitLibrary.dsiList.add(fil.dsi);
    }

    String fname = "out/fred-series-info.csv";
    FredUtils.writeSeriesInfoCsv(FredInitLibrary.dsiList, fname);

  }

  /**
   * 
   * @param dt
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static int process(DateTime dt) throws FileNotFoundException, IOException {

    int unprocessed = 0;
    int processed = 0;
    int errors = 0;

    for (final FredInitLibrary fil : FredInitLibrary.filList) {
      if (!fil.isComplete()) {

        Debug.LOGGER.info(String.format("%n-----%nProcessing : %s", fil.getName()));
        if (fil.isDsiValid()) {
          Debug.LOGGER.info(String.format("Processing previously completed for DSI : %s", fil.getName()));
        }
        if (fil.isDsValid()) {
          Debug.LOGGER.info(String.format("Processing previously completed for DS : %s", fil.getName()));
        }

        if (fil.dsi == null) {
          Debug.LOGGER.info(String.format("Processing DSI : %s", fil.getName()));
          final DataSeriesInfo dsi = new DataSeriesInfo(fil.name, dt);
          if (dsi.isValid()) {
            Debug.LOGGER.info(String.format("DSI Set%n%s", dsi));
            fil.dsi = dsi;
            Debug.LOGGER.info(String.format("Processing DS : %s", fil.getName()));
            final DataSeries ds = new DataSeries(fil.dsi);
            if (ds.isValid()) {
              fil.ds = ds;
              processed++;
              Debug.LOGGER.info(String.format("DS Set : %s  processed=%d  unprocessed=%d%n%s", fil.getName(), processed, unprocessed, ds));
            }
            else {
              Debug.LOGGER.info(String.format("Failed Processing DS : %s   processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));
              unprocessed++;
              errors++;
            }
          }
          else {
            Debug.LOGGER.info(String.format("Failed Processing DSI : %s   processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));
            unprocessed++;
            errors++;
          }
        }
        else if (fil.ds == null) {
          final DataSeries ds = new DataSeries(fil.dsi);
          if (ds.isValid()) {
            fil.ds = ds;
            processed++;
            Debug.LOGGER.info(String.format("DS Set : %s  processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));
          }
          else {
            unprocessed++;
            errors++;
            Debug.LOGGER.info(String.format("Failed Processing DS : %s   processed=%d  unprocessed=%d", fil.getName(), processed, unprocessed));

          }
        }
      }
      if (errors == 5) {
        Debug.LOGGER
            .info(String.format("%nProcessing errors=%d. Pausing for %d seconds.  unprocessed=%d%n", errors, FredInitLibrary.longPause, unprocessed));
        errors = 0;
        Utils.sleep(FredInitLibrary.longPause * 1000);
      }
    }
    return unprocessed;
  }

  public static void writeToLib(FredInitLibrary fil) {

    if (!fil.isComplete()) {
      return;
    }

    final double scaler = FredUtils.getScaler(fil.dsi.getUnits());

    final String fullFileName = FredUtils.toFullFileName(ftLibDir, fil.dsi.getName(), fil.dsi.getTitle());

    final String ffn = fullFileName.replace(">", "greater");
    final File file = new File(ffn);
    final File fileshort = new File(ftLibDir + "/" + fil.dsi.getName() + ".csv");

    try (PrintWriter pw = new PrintWriter(file); PrintWriter pwShort = new PrintWriter(fileshort)) {
      pw.println("Date," + fil.dsi.getFileDt());
      pwShort.println("Date," + fil.dsi.getName());
      for (final DataValues dv : fil.ds.getDvList()) {
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

  private final String  name;
  public DataSeriesInfo dsi;
  private DataSeries    ds;

  public FredInitLibrary(String n) {
    this.name = n;
    this.dsi = null;
    this.ds = null;
  }

  public DataSeries getDs() {
    return this.ds;
  }

  public DataSeriesInfo getDsi() {
    return this.dsi;
  }

  public String getName() {
    return this.name;
  }

  public boolean isComplete() {
    boolean ret = false;
    if (this.dsi != null && this.ds != null) {
      if (this.dsi.isValid()) {
        if (this.ds.isValid()) {
          ret = true;
        }
      }
    }
    return ret;
  }

  public boolean isDsiValid() {
    boolean ret = false;
    if (this.dsi != null) {
      ret = this.dsi.isValid();
    }
    return ret;
  }

  public boolean isDsValid() {
    boolean ret = false;
    if (this.ds != null) {
      ret = this.ds.isValid();
    }
    return ret;
  }

  @Override
  public String toString() {
    String ret = String.format("Series     : %s%n", this.name);
    if (this.dsi != null) {
      ret += String.format(" DSI Valid : %s%n", this.dsi.isValid());
    }
    if (this.ds != null) {
      ret += String.format(" DS Valid  : %s%n", this.ds.isValid());
    }
    ret += String.format(" Complete  : %s", this.isComplete());
    return ret;
  }

}
