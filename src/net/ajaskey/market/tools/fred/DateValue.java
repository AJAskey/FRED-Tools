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

import net.ajaskey.common.DateTime;

public class DateValue {

  private DateTime date;
  private double   value;
  private boolean  valid;

  /**
   * 
   * This method serves as a constructor for the class.
   *
   * @param sDate
   * @param sVal
   */
  public DateValue(final String sDate, final String sVal) {

    this.valid = true;
    try {
      if (sDate.length() == 10) {
        this.date = new DateTime(sDate, "yyyy-MM-dd");
      }
      else {
        this.valid = false;
      }
      this.value = Double.parseDouble(sVal);
    }
    catch (final Exception e) {
      this.value = 0.0;
      this.valid = false;
    }
  }

  public DateValue(final DateTime dt, final double val) {

    this.valid = true;
    try {
      this.date = new DateTime(dt);
      this.value = val;
    }
    catch (final Exception e) {
      this.value = 0.0;
      this.valid = false;
    }
  }

  /**
   * @return the date
   */
  public DateTime getDate() {

    return this.date;
  }

  /**
   * @return the value
   */
  public double getValue() {

    return this.value;
  }

  @Override
  public String toString() {

    return String.format("%s\t%f", this.date, this.value);
  }

  public boolean isValid() {
    return valid;
  }
}
