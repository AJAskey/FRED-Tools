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

package net.ajaskey.market.tools.fred;

public class DataSeries {

  public enum AggregationMethodType {
    AVG, EOP, SUM
  }

  public enum FileType {
    JSON, TXT, XLS, XML
  }

  public enum OrderType {
    ASC, DESC
  }

  /*
   * lin = Levels (No transformation)
   *
   * chg = Change ch1 = Change from Year Ago
   *
   * pch = Percent Change
   *
   * pc1 = Percent Change from Year Ago
   *
   * pca = Compounded Annual Rate of Change
   *
   * cch = Continuously Compounded Rate of Change
   *
   * cca = Continuously Compounded Annual Rate of Change log = Natural Log
   */
  public enum ResponseType {
    CCA, CCH, CH1, CHG, LIN, LOG, PC1, PCA, PCH
  }

}
