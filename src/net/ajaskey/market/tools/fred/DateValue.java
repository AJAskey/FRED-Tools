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

import java.text.SimpleDateFormat;
import java.util.Date;

import net.ajaskey.common.DateTime;

public class DateValue {

  public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  public DateTime date;
  public boolean  valid;

  public double value;

  /**
   * This method serves as a constructor for the class.
   *
   * @param dv
   */
  public DateValue(final DateValue dv) {

    this.date = dv.date;
    this.value = dv.value;
    this.valid = true;
  }

  /**
   * This method serves as a constructor for the class.
   *
   */
  public DateValue(final String str, final int fptr) {

    this.valid = false;
    try {
      if (str.trim().length() > 10) {
        final String fld[] = str.trim().split(",");
        if (fld.length > 1) {
          final Date dat = DateValue.sdf.parse(fld[0].trim());
          this.date = new DateTime(dat);
          final String field = fld[fptr].replaceAll("\"", "").replaceAll(",", "").trim();
          this.value = Double.parseDouble(field);
          this.valid = true;
        }
      }
    }
    catch (final Exception e) {
      this.date = null;
      this.value = 0.0;
      this.valid = false;
    }
  }

  @Override
  public String toString() {

    final String ret = String.format("%s\t%.2f", this.date, this.value);
    return ret;
  }
}
