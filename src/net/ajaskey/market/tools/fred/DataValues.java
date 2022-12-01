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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.ajaskey.common.DateTime;

public class DataValues {

  public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  private final DateTime date;

  private double value;

  /**
   * This method serves as a constructor for the class.
   *
   * @param cal
   * @param val
   */
  public DataValues(final DateTime dt, final double val) {

    this.date = new DateTime(dt);
    this.value = val;
  }

  /**
   * This method serves as a constructor for the class.
   *
   */
  public DataValues(final String date, final String val) {

    this.date = new DateTime();

    this.setDate(date);
    this.setValue(val);
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

  /**
   * @param sdf. the date to set
   */
  public void setDate(final String dateStr) {

    try {
      final Date d = DataValues.sdf.parse(dateStr);
      this.date.set(d);
    }
    catch (final ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param value the value to set
   */
  public void setValue(final String val) {

    try {
      this.value = Double.parseDouble(val);
    }
    catch (final Exception e) {
      this.value = 0.0;
    }
  }

  @Override
  public String toString() {

    return String.format("%s\t%f", this.date, this.value);
  }
}
