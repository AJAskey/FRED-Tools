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
package net.ajaskey.market.tools.pricedata;

public class TickerData {

  private String    code;
  private PriceData data;
  private boolean   valid;

  public TickerData() {
    this.code = "Unknown";
    this.data = null;
    this.valid = false;
  }

  public TickerData(String c, PriceData pd) {
    if (pd != null) {
      this.valid = true;
      this.code = c;
      this.data = pd;
    }
    else {
      this.valid = false;
    }
  }

  public String getCode() {
    return this.code;
  }

  public PriceData getPriceData() {
    return this.data;
  }

  public boolean isValid() {
    return this.valid;
  }

  @Override
  public String toString() {
    final String ret = String.format("%s%n%s", this.code, this.data);
    return ret;
  }

}
