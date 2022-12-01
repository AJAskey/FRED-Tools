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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.ajaskey.common.DateTime;

public class SpxVsAll {

  // private static TickerPriceData spxData = new TickerPriceData("WI", "SPX");

  /**
   * net.ajaskey.market.tools.fred.processing.getSpxClose
   *
   * @param date
   * @return
   */
//	private static double getSpxClose(final DateTime date) {
//
//		//System.out.printf("%s\t%s%n", Utils.sdf.format(date.getTime()), Utils.sdf.format(spxPrices.get(0).date.getTime()));
//
//		boolean b = date.isLessThan(spxPrices.get(0).date);
//		if (b) {
//			return 0.0;
//		}
//
//		for (final PriceData pd : spxPrices) {
//
//			//System.out.printf("%s\t%s%n", Utils.sdf.format(date.getTime()), Utils.sdf.format(pd.date.getTime()));
//
//			if (pd.date.isGreaterThanOrEqual(date)) {
//				return pd.close;
//			}
//		}
//		return 0;
//	}

  /**
   *
   * net.ajaskey.market.tools.fred.processing.main
   *
   * @param args
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void main(final String[] args) throws FileNotFoundException, IOException {

    // final TickerPriceData spxData = new TickerPriceData("WI", "SPX");

    final double lastSpxPrice = 4000.0; // spxData.getLatest();
    final DateTime lastSpxDate = new DateTime(); // spxData.getLatestDate();
    System.out.println("SPX latest date  : " + lastSpxDate);
    System.out.println("SPX latest price : " + lastSpxPrice);

    final File folder = new File(FredCommon.fredPath);

    final List<String> codeList = new ArrayList<>();

    // codeList.add("SP500_Earnings");

    codeList.add("[GDP]");
    codeList.add("[CP]");
    codeList.add("[INDPRO]");
    codeList.add("[TTLCON]");
    codeList.add("[PAYNSA]");
    codeList.add("[TOTCINSA]");
    codeList.add("[PPIACO]");
    codeList.add("[TOTBUSSMNSA]");
    codeList.add("[RETAILSMNSA]");
    codeList.add("[WHLSLRSMNSA]");
    codeList.add("[MNFCTRSMNSA]");
    codeList.add("[A939RC0Q052SBEA]");
    codeList.add("[TNWBSHNO]");
    codeList.add("[UMTMNO]");
    codeList.add("[TOTALNS]");
    codeList.add("[GPDI]");
    codeList.add("[HSN1FNSA]");
    codeList.add("[TLMFGCON]");
    codeList.add("[AMBNS]");
    codeList.add("[CAPUTLB5640CS]");
    codeList.add("[FRGSHPUSM649NCIS]");
    codeList.add("[BOGZ1FU385050005Q]");
    codeList.add("[RU2000PR]");

    final List<File> fileList = new ArrayList<>();

    final String[] ext = new String[] { "csv" };
    final List<File> files = (List<File>) FileUtils.listFiles(folder, ext, false);
    for (final File file : files) {
      fileList.add(file);
    }
    // File pefile = new File("d:\\Data2\\MA\\CSV
    // Data\\Quandl\\SP500_Earnings.csv");
    // fileList.add(pefile);

    for (final String s : codeList) {

      for (final File f : fileList) {

        if (f.getName().contains(s)) {

          // System.out.println(f.getAbsolutePath());
          final List<OptumaFileData> ofd = IngestOptumaFile.readDataFile(f.getAbsolutePath());

          final List<OptumaFileData> mergedData = SpxVsAll.merge(ofd);

          final String fname = f.getAbsolutePath();
          String outfile = "";
          if (fname.contains("Quandl")) {
            outfile = "d:\\Data2\\MA\\CSV Data\\Quandl\\SPX Growth vs SP500 Earnings Growth.csv";
          }
          else {
            outfile = fname.replace("\\[", "\\[SPX Growth vs ").replace("- ", "- SPX Growth vs ").replace("]", " Growth]").replace(".csv",
                " Growth.csv");
          }
          System.out.println(outfile);

          try (PrintWriter pw = new PrintWriter(outfile)) {

            for (final OptumaFileData d : mergedData) {
              pw.println(d);
            }
          }

        }
      }
    }

  }

  /**
   * net.ajaskey.market.tools.fred.processing.merge
   *
   * @param spxPrices
   * @param of
   * @return
   */
  private static List<OptumaFileData> merge(final List<OptumaFileData> ofd) {

    final List<OptumaFileData> ret = new ArrayList<>();

    double totalChg = 1.0;

    for (int i = 1; i < ofd.size(); i++) {

      double price1 = 4000.0; // SpxVsAll.spxData.getClose(ofd.get(i - 1).date);
      double price2 = 4000.0; // SpxVsAll.spxData.getClose(ofd.get(i).date);

      // System.out.println(ofd.get(i).date);

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
        ret.add(out);
      }
    }

    return ret;
  }
}
