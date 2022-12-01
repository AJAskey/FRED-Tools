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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.ajaskey.common.DateTime;
import net.ajaskey.market.tools.fred.DateValue;
import net.ajaskey.market.tools.fred.DateValueSorter;

public class ParseTreastData {

  /**
   * net.ajaskey.market.tools.fred.main
   *
   * @param args
   * @throws IOException5
   */
  public static void main(final String[] args) throws IOException {

    final List<DateValue> pastDvList = ParseTreastData.readFile(FredCommon.fredPath + "TREAST.csv", 1);
    final List<DateValue> FutureDvList = ParseTreastData.readFile("TREAST\\SomaNonMbs-121819.csv", 6);

    System.out.println(FutureDvList.size());

    ParseTreastData.process(pastDvList, FutureDvList);

  }

  /**
   *
   * net.ajaskey.market.tools.fred.process
   *
   * @param pastDvList
   * @param futureDvList
   * @throws FileNotFoundException
   */
  public static void process(final List<DateValue> pastDvList, final List<DateValue> futureDvList) throws FileNotFoundException {

    final List<DateValue> cumList = new ArrayList<>();

    // Process TREAST data
    for (final DateValue dv : futureDvList) {
      boolean found = false;
      for (final DateValue cdv : cumList) {
        if (dv.date.isEqual(cdv.date)) {
          cdv.value += dv.value;
          found = true;
          break;
        }
      }
      if (!found) {
        final DateValue dvv = new DateValue(dv);
        cumList.add(dvv);
      }
    }

    Collections.sort(cumList, new DateValueSorter());
    for (final DateValue dv : cumList) {
      System.out.println(dv);
    }

    double lastValue = 0.0;
    final DateTime lastDate = new DateTime();

    // Write TREAST data
    try (PrintWriter pw = new PrintWriter(FredCommon.fredPath + "/TREAST-ALL.csv");
        PrintWriter pw2019 = new PrintWriter(FredCommon.fredPath + "/TREAST-2019.csv");) {

      final DateTime end2019 = new DateTime(2020, DateTime.JANUARY, 1);
      for (final DateValue dv : pastDvList) {
        final String d = dv.date.format("yyyy-MM-dd");
        pw.printf("%s,%.1f%n", d, dv.value);

        pw2019.printf("%s,%.1f%n", d, dv.value);
        lastDate.set(dv.date);
        lastValue = dv.value;
      }

      for (final DateValue dv : cumList) {
        if (dv.date.isGreaterThan(lastDate)) {

          final String d = dv.date.format("yyyy-MM-dd");
          lastValue -= dv.value;

          pw.printf("%s,%.1f%n", d, lastValue);

          if (dv.date.isLessThanOrEqual(end2019)) {
            pw2019.printf("%s,%.1f%n", d, lastValue);
          }

        }
      }
    }
  }

  /**
   *
   * net.ajaskey.market.tools.fred.readFile
   *
   * @param fname
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static List<DateValue> readFile(final String fname, final int fldptr) throws FileNotFoundException, IOException {

    final List<DateValue> dvList = new ArrayList<>();

    final File f = new File(fname);
    if (f.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(f))) {

        String line;
        while ((line = reader.readLine()) != null) {
          final String str = line.trim();
          final DateValue dv = new DateValue(str, fldptr);
          if (dv.valid) {
            dvList.add(dv);
          }
        }
      }
    }

    return dvList;
  }

}
