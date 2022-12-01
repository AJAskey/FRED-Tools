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

import net.ajaskey.common.DateTime;

//import DateTime;

public class PriceData {

  public PriceData(String c) {
    // TODO Auto-generated constructor stub
  }

  public double   close;
  public DateTime date;
  public double   high;
  public double   low;
  public double   open;
  public long     volume;
  private boolean valid;

  /**
   * This method serves as a constructor for the class.
   * 
   * @param dt DateTime of data
   * @param o  Open price
   * @param h  High price
   * @param l  Low price
   * @param c  Close price
   * @param v  Volume
   */
  public PriceData(final DateTime dt, final double o, final double h, final double l, final double c, final long v) {

    try {
      this.date = new DateTime(dt);
      this.open = o;
      this.high = h;
      this.low = l;
      this.close = c;
      this.volume = v;
      this.valid = true;
    }
    catch (Exception e) {
      this.valid = false;
    }
  }

  public boolean isValid() {

    // valid = this.date.isValid();

    return this.valid;
  }

  public String toShortString() {

    final String ret = String.format("%s, %.2f", this.date.format("yyyy-MM-dd"), this.close);
    return ret;
  }

  public String toShortString(double scaler) {

    final String ret = String.format("%s, %.2f", this.date.format("yyyy-MM-dd"), this.close / scaler);
    return ret;
  }

  public String toOptumaString(String desc, double scaler) {

    final String ret = String.format("%s, %s, %.2f, %.2f, %.2f, %.2f, %d, 0", desc, this.date.format("yyyyMMdd"), this.open / scaler,
        this.high / scaler, this.low / scaler, this.close / scaler, this.volume);
    return ret;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    final String ret = String.format("%s, %.2f, %.2f, %.2f, %.2f, %10d", this.date.format("yyyy-MM-dd"), this.open, this.high, this.low, this.close,
        this.volume);
    return ret;
  }

}
