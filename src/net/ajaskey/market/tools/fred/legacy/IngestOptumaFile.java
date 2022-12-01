/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Original author : Andy Askey (ajaskey34@gmail.com)
 */

package net.ajaskey.market.tools.fred.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.ajaskey.common.DateTime;

public class IngestOptumaFile {

  public static final int ADD = 1;

  public static final double     BAD_OFD_DATA = -666.600;
  public static final int        DIVIDE       = 4;
  public static final int        MULTIPLY     = 3;
  public static SimpleDateFormat sdf          = new SimpleDateFormat("yyyy-MM-dd");

  public static final int SUBTRACT = 2;

  /**
   * Testout only
   *
   * net.ajaskey.market.tools.fred.processing.main
   *
   * @param args
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void main(final String[] args) throws FileNotFoundException, IOException {

    // IngestOptumaFile.spxData = new TickerPriceData("WI", "SPX");

    final File f1 = new File("d:\\Data2\\MA\\CSV Data\\Fred-Download\\GDP.csv");
    final File f2 = new File("d:\\Data2\\MA\\CSV Data\\Fred-Download\\TTLCON.csv");

    final IngestOptumaFile iof = new IngestOptumaFile(f2, f1);
    final List<OptumaFileData> mergedData = iof.processFiles(IngestOptumaFile.DIVIDE, 1.0);

    final String fname = "d:\\Data2\\MA\\CSV Data\\Fred-Download\\GDP vs TTLCON.csv";

    try (PrintWriter pw = new PrintWriter(fname)) {
      for (final OptumaFileData d : mergedData) {
        pw.println(d);
      }
    }

  }

  /**
   *
   * Interface to world. net.ajaskey.market.tools.fred.processing.process
   *
   * @param f1name
   * @param f2name
   * @param title
   * @param operation
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void process(final String f1name, final String f2name, final String title, final int operation, double scaler)
      throws FileNotFoundException, IOException {

    System.out.printf("%s\t%s%n", f1name, f2name);
    final IngestOptumaFile iof = new IngestOptumaFile(f1name, f2name);
    final List<OptumaFileData> resultsList = iof.processFiles(operation, scaler);

    final String fname = String.format("%s%s", FredCommon.fredPath, title);
    try (PrintWriter pw = new PrintWriter(fname)) {
      for (final OptumaFileData ofd : resultsList) {
        pw.println(ofd);
      }
    }
  }

  /**
   *
   * net.ajaskey.market.tools.fred.processing.readDataFile
   *
   * @param fname
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static List<OptumaFileData> readDataFile(final String fname) throws FileNotFoundException, IOException {

    final List<OptumaFileData> ret = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {

      boolean firstOne = true;
      String line;
      while ((line = reader.readLine()) != null) {
        final String str = line.trim();
        final String fld[] = str.split(",");
        try {
          final Date d = IngestOptumaFile.sdf.parse(fld[0].trim());
          final DateTime dt = new DateTime(d);
          final double val = Double.parseDouble(fld[1]);
          if (firstOne && Math.abs(val) == 0.0) {
            // noop
          }
          else {
            firstOne = false;
            ret.add(new OptumaFileData(dt, val));
          }
        }
        catch (final ParseException e) {
        }
      }
    }
    return ret;
  }

  public List<OptumaFileData> mergedPercents = new ArrayList<>();

  List<OptumaFileData> diffList = new ArrayList<>();

  List<OptumaFileData> f1List = new ArrayList<>();

  List<OptumaFileData> f2List = new ArrayList<>();

  File file1 = null;

  File   file2 = null;
  String title;

  /**
   *
   * This method serves as a constructor for the class.
   *
   * @param f1
   * @param f2
   * @param string
   * @param subtract2
   */
  private IngestOptumaFile(final File f1, final File f2) {

    this.file1 = f1;
    this.file2 = f2;
  }

  /**
   * This method serves as a constructor for the class.
   *
   */
  private IngestOptumaFile(final String f1, final String f2) {

    this.file1 = new File(f1);
    this.file2 = new File(f2);
  }

  /**
   *
   * net.ajaskey.market.tools.fred.processing.mergePercents
   *
   * @param ofd
   */
  public void mergePercents(final List<OptumaFileData> ofd) {

    double totalChg = 1.0;

    for (int i = 1; i < ofd.size(); i++) {

      double price1 = 4000.0; // IngestOptumaFile.spxData.getClose(ofd.get(i - 1).date);
      double price2 = 4000.0; // IngestOptumaFile.spxData.getClose(ofd.get(i).date);

      if (Math.abs(price1) > 0.0 && Math.abs(price2) > 0.0) {

        final double pChg = (price2 - price1) / price1;

        price1 = ofd.get(i - 1).val;
        price2 = ofd.get(i).val;

        double vChg = 0.0;
        if (Math.abs(price1) > 0.0 && Math.abs(price2) > 0.0) {
          vChg = (price2 - price1) / price1;
          totalChg += pChg - vChg;
        }

        // System.out.printf("%.3f\t %.3f\t%.3f\t%.3f\t%.3f%n", pChg, vChg, totalChg,
        // price1, price2);

        final OptumaFileData out = new OptumaFileData(ofd.get(i).date, totalChg);
        this.mergedPercents.add(out);
      }
    }

    // return ret;
  }

  /**
   * net.ajaskey.market.tools.fred.processing.interpolate
   *
   * @param date
   * @param f2List2
   * @return
   */
  private double interpolate(final DateTime baseDate, final List<OptumaFileData> fList) {

    if (baseDate.isLessThan(fList.get(0).date)) {
      return IngestOptumaFile.BAD_OFD_DATA;
    }

    for (final OptumaFileData element : fList) {

      // System.out.printf("%s\t%s%n", Utils.sdf.format(baseDate.getTime()),
      // Utils.sdf.format(fList.get(i).date.getTime()));

      if (baseDate.isGreaterThanOrEqual(element.date)) {
        return element.val;
      }
    }

    return IngestOptumaFile.BAD_OFD_DATA;
  }

  /**
   *
   * net.ajaskey.market.tools.fred.processing.processFiles
   *
   * @param operation
   * @param scaler
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  private List<OptumaFileData> processFiles(final int operation, double scaler) throws FileNotFoundException, IOException {

    final List<OptumaFileData> resultList = new ArrayList<>();

    this.f1List = this.readFile(this.file1);
    this.f2List = this.readFile(this.file2);

    // System.out.printf("%d\t%d%n", this.f1List.size(), this.f2List.size());

    int f1size = this.f1List.size();
    int f2size = this.f2List.size();

    /**
     * Merge lists of different sizes with f1List as the base.
     */
    if (f1size != f2size) {

      final List<OptumaFileData> fList = new ArrayList<>();
      final List<OptumaFileData> fBaseList = new ArrayList<>();

      for (final OptumaFileData ofd : this.f1List) {
        final double price2 = this.interpolate(ofd.date, this.f2List);
        if (price2 != IngestOptumaFile.BAD_OFD_DATA) {
          final OptumaFileData d = new OptumaFileData(ofd.date, price2);
          fList.add(d);
          final OptumaFileData b = new OptumaFileData(ofd.date, ofd.val);
          fBaseList.add(b);
        }
      }
      this.f2List.clear();
      this.f2List = new ArrayList<>(fList);
      f2size = this.f2List.size();

      this.f1List.clear();
      this.f1List = new ArrayList<>(fBaseList);
      f1size = this.f1List.size();
    }

    if (f1size == f2size) {
      for (int i = 0; i < f1size; i++) {
        double result = 0.0;
        try {
          if (operation == IngestOptumaFile.ADD) {
            result = this.f1List.get(i).val + this.f2List.get(i).val;
          }
          else if (operation == IngestOptumaFile.SUBTRACT) {
            result = this.f1List.get(i).val - this.f2List.get(i).val;
          }
          else if (operation == IngestOptumaFile.MULTIPLY) {
            result = this.f1List.get(i).val * this.f2List.get(i).val;
          }
          else if (operation == IngestOptumaFile.DIVIDE) {
            result = this.f1List.get(i).val / this.f2List.get(i).val;
          }
        }
        catch (final Exception e) {
          result = 0.0;
        }
        final OptumaFileData ofd = new OptumaFileData(this.f1List.get(i).date, result * scaler);
        resultList.add(ofd);
      }
    }

    return resultList;

  }

  /**
   * net.ajaskey.market.tools.fred.readFile
   *
   * @param f1
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   */
  private List<OptumaFileData> readFile(final File f) throws FileNotFoundException, IOException {

    return IngestOptumaFile.readDataFile(f.getAbsolutePath());
  }

}
