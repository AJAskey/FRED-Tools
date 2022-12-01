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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class HiresSeps {

  /**
   * net.ajaskey.market.tools.fred.main
   *
   * @param args
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(final String[] args) throws FileNotFoundException, IOException {

    final File folder = new File(FredCommon.fredPath);

    final List<String> jtuList = new ArrayList<>();

    final String[] ext = new String[] { "csv" };
    final List<File> files = (List<File>) FileUtils.listFiles(folder, ext, false);
    for (final File file : files) {
      final String name = file.getName();
      if (name.startsWith("JTU")) {
        jtuList.add(name);
      }
    }

    for (final String s1 : jtuList) {
      if (s1.contains("HIL.csv")) {
        final String tmp1 = s1.replace("HIL.csv", "").trim();
        for (final String s2 : jtuList) {
          if (s2.contains("TSL.csv")) {
            final String tmp2 = s2.replace("TSL.csv", "");
            if (tmp2.equals(tmp1)) {
              final String fullname = HiresSeps.findFullName(tmp1, files);
              System.out.println(fullname);
              IngestOptumaFile.process(FredCommon.fredPath + s1, FredCommon.fredPath + s2, fullname, IngestOptumaFile.SUBTRACT, 1.0);
            }
          }
        }
      }
    }
  }

  /**
   * net.ajaskey.market.tools.fred.findFullName
   *
   * @param tmp1
   * @param files
   * @return
   */
  private static String findFullName(final String title, final List<File> files) {

    for (final File file : files) {
      final String name = file.getName();
      if (name.startsWith("[JTU")) {
        if (name.contains("HIL]")) {
          if (name.contains(title)) {
            final String ret = file.getName().replace("HIL]", "DIFF]").replace("Hires", "HiresMinusSeparations");
            return ret;
          }
        }
      }
    }

    return null;
  }
}
