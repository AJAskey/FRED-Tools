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
package net.ajaskey.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

  static final int BUFFER = 2048;

  /**
   *
   * @param dirs
   * @param files
   * @param outdir
   * @param outfile
   */
  public static void create(final List<String> dirs, final List<String> files, final String outdir, final String outfile) {

    try {

      final FileOutputStream dest = new FileOutputStream(outdir + "\\" + outfile);
      final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

      final List<File> fileList = new ArrayList<>();

      BufferedInputStream origin = null;
      out.setMethod(ZipOutputStream.DEFLATED);
      final byte data[] = new byte[Zip.BUFFER];

      /**
       * Add files from directories
       */
      for (final String dir : dirs) {
        final File[] fils = new File(dir).listFiles();

        for (final File ff : fils) {
          fileList.add(ff);
        }
      }

      /**
       * Add individual files
       */
      for (final String fil : files) {
        fileList.add(new File(fil));
      }

      /**
       * Process list of files and add to zip
       */
      for (final File ff : fileList) {
        // System.out.println("Adding: " + ff.getAbsolutePath());

        final FileInputStream fi = new FileInputStream(ff);
        origin = new BufferedInputStream(fi, Zip.BUFFER);

        final ZipEntry entry = new ZipEntry(ff.getPath());
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, Zip.BUFFER)) != -1) {
          out.write(data, 0, count);
        }
        origin.close();
      }

      out.close();
    }
    catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param argv
   */
  public static void main(final String argv[]) {

    final List<String> dir = new ArrayList<>();
    dir.add("output\\SIP");
    final List<String> fil = new ArrayList<>();
    fil.add("data\\SP-Stocks.txt");
    fil.add("data\\SP-Stocks-B.txt");

    Zip.create(dir, fil, "archive", "newSip.zip");

  }
}
