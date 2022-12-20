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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;

/**
 * This class contains various utility methods for process text
 */
public class TextUtils {

  /**
   * Prints a List of String to Standard output
   *
   * @param list List of String
   */
  public static void print(List<String> list) {
    System.out.println(TextUtils.toString(list));
  }

  /**
   * Prints a String to Standard output
   *
   * @param s String to print
   */
  public static void printline(String s) {
    System.out.println(s);
  }

  /**
   * Reads a file that has been Gzipped
   *
   * @param file         File
   * @param ignoreBlanks Does not return blank lines from file if TRUE
   * @return List of String
   */
  public static List<String> readGzipFile(File file, boolean ignoreBlanks) {

    if (!file.exists()) {
      return null;
    }

    List<String> retList = new ArrayList<>();

    try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
        BufferedReader br = new BufferedReader(new InputStreamReader(gzip));) {

      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.trim().length() > 0) {
          retList.add(line);
        }
      }
    }
    catch (final Exception e) {
      retList = null;
    }
    return retList;
  }

  /**
   * Reads a file that has been Gzipped
   *
   * @param fname        File name
   * @param ignoreBlanks Does not return blank lines from file if TRUE
   * @return List of String
   */
  public static List<String> readGzipFile(String fname, boolean ignoreBlanks) {

    final File file = new File(fname);

    return TextUtils.readGzipFile(file, ignoreBlanks);
  }

  /**
   * Reads a text file
   *
   * @param file         File
   * @param ignoreBlanks Does not return blank lines from file if TRUE
   * @return List of String
   */
  public static List<String> readTextFile(File file, boolean ignoreBlanks) {

    final List<String> ret = new ArrayList<>();

    if (!file.exists()) {
      return ret;
    }

    List<String> lines = null;

    try {
      lines = FileUtils.readLines(file, "UTF-8");
    }
    catch (final IOException e) {
      return ret;
    }

    if (ignoreBlanks) {
      for (final String s : lines) {
        if (s.trim().length() > 0) {
          ret.add(s.trim());
        }
      }
    }
    else {
      return lines;
    }
    return ret;
  }

  /**
   * Reads a text file
   *
   * @param fname        File name
   * @param ignoreBlanks Does not return blank lines from file if TRUE
   * @return List of String
   */
  public static List<String> readTextFile(String fname, boolean ignoreBlanks) {

    final File f = new File(fname);

    return TextUtils.readTextFile(f, ignoreBlanks);
  }

  /**
   * Returns a newline delimited String from List of String
   *
   * @param list List of String
   * @return String
   */
  public static String toString(List<String> data) {
    String ret = "";
    try {
      for (final String s : data) {
        ret += s + Utils.NL;
      }
    }
    catch (final Exception e) {
      ret = "";
    }
    return ret;
  }
}
