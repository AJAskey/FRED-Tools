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

public class MathUtil {

  public static void main(String[] args) {
    double d = 2.94;
    double rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 2.99;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.10;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.11;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.12;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.13;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.14;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.15;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.16;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.17;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.18;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.19;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

    d = 3.20;
    rd = MathUtil.Mround(d, 0.05);
    System.out.printf("%.2f\t%.2f%n", d, rd);

  }

  /**
   *
   * @param val   Value to round
   * @param scale Value for rounding (0.05 == a nickel)
   * @return
   */
  public static double Mround(double val, double scale) {

    return Math.floor(val / scale + 0.5) * scale;

  }

}
