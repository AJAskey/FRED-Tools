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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTime implements Serializable {

  public static final int   APRIL            = Calendar.APRIL;
  public static final int   AUGUST           = Calendar.AUGUST;
  public static final int   DATE             = Calendar.DATE;
  public static final int   DECEMBER         = Calendar.DECEMBER;
  public static final int   FEBRUARY         = Calendar.FEBRUARY;
  public static final int   FRIDAY           = Calendar.FRIDAY;
  public static final int   HOUR             = Calendar.HOUR;
  public static final int   HOUR_OF_DAY      = Calendar.HOUR_OF_DAY;
  public static final int   JANUARY          = Calendar.JANUARY;
  public static final int   JULY             = Calendar.JULY;
  public static final int   JUNE             = Calendar.JUNE;
  public static final int   MARCH            = Calendar.MARCH;
  public static final int   MAY              = Calendar.MAY;
  public static final int   MILLISECOND      = Calendar.MILLISECOND;
  public static final int   MINUTE           = Calendar.MINUTE;
  public static final int   MONDAY           = Calendar.MONDAY;
  public static final int   MONTH            = Calendar.MONTH;
  public static final int   NOVEMBER         = Calendar.NOVEMBER;
  public static final int   OCTOBER          = Calendar.OCTOBER;
  public static final int   SATURDAY         = Calendar.SATURDAY;
  public static final int   SECOND           = Calendar.SECOND;
  public static final int   SEPTEMBER        = Calendar.SEPTEMBER;
  public static final int   SUNDAY           = Calendar.SUNDAY;
  public static final int   THURSDAY         = Calendar.THURSDAY;
  public static final int   TUESDAY          = Calendar.TUESDAY;
  public static final int   WEDNESDAY        = Calendar.WEDNESDAY;
  public static final int   YEAR             = Calendar.YEAR;
  private static final long serialVersionUID = -4689128633349138281L;

  /**
   *
   * net.ajaskey.market.misc.copy
   *
   * @param dt Instantiated DateTime variable
   * @return
   */
  public static DateTime copy(final DateTime dt) {
    try {
      if (!dt.isNull()) {
        final DateTime d = new DateTime(dt.cal.getTime());
        return d;
      }
    }
    catch (final Exception e) {
    }
    return null;
  }

  /**
   *
   * net.ajaskey.market.misc.main
   *
   * @param args
   */
  public static void main(final String[] args) {
    final DateTime dt = new DateTime();
    final DateTime dt2 = new DateTime();
    dt2.add(DateTime.DATE, 1);
    System.out.println(dt);
    System.out.println(dt2);
    boolean b = dt.isLessThan(dt2);
    System.out.println("dt < dt2 : " + b);
    b = dt2.isLessThan(dt);
    System.out.println("dt2 < dt : " + b);
    b = dt.isGreaterThan(dt2);
    System.out.println("dt > dt2 : " + b);
    b = dt2.isGreaterThan(dt);
    System.out.println("dt2 > dt : " + b);
    System.out.println(dt.getSdf().toPattern());
    System.out.println(dt.getMonth());
    System.out.println(dt.getDay());
    System.out.println(dt.getYear());
    System.out.println(dt.getDayOfYear());
    System.out.println(dt.getDayOfWeek());
    System.out.println(dt.getDayOfMonth());
    final String s = dt.format("yyyy-MM-dd");
    System.out.println(s);
    dt.setSdf(Utils.sdfFull);
    System.out.println(dt);
    System.out.println(dt.getSdf().toPattern());
    System.out.println(dt.isWeekday());
    dt.set(2019, DateTime.MAY, 5);
    System.out.println(dt);
    System.out.println(dt.isWeekday());
    dt.add(DateTime.DATE, 3);
    System.out.println(dt);
    dt.add(DateTime.MONTH, -13);
    System.out.println(dt);
  }

  /**
   *
   * @param dt1
   * @param dt2
   * @return
   */
  public static boolean sameDate(final DateTime dt1, final DateTime dt2) {
    try {
      if (dt1.isNull() || dt2.isNull()) {
        return false;
      }
      if (dt1.cal.get(Calendar.YEAR) == dt2.cal.get(Calendar.YEAR)) {
        if (dt1.cal.get(Calendar.MONTH) == dt2.cal.get(Calendar.MONTH)) {
          if (dt1.cal.get(Calendar.DATE) == dt2.cal.get(Calendar.DATE)) {
            return true;
          }
        }
      }
    }
    catch (final Exception e) {
      return false;
    }
    return false;
  }

  private Calendar cal = null;

  private SimpleDateFormat sdf = null;

  /**
   * This method serves as a constructor for the class.
   *
   */
  public DateTime() {
    this.cal = Calendar.getInstance();
  }

  /**
   *
   * This method serves as a constructor for the class.
   *
   * @param c Instantiated Calendar variable
   */
  public DateTime(final Calendar c) {
    this.cal = Calendar.getInstance();
    try {
      this.cal.setTime(c.getTime());
    }
    catch (final Exception e) {
    }
  }

  /**
   *
   * This method serves as a constructor for the class.
   *
   * @param d Instantiated Date variable
   */
  public DateTime(final Date d) {
    this.cal = Calendar.getInstance();
    try {
      this.cal.setTime(d);
    }
    catch (final Exception e) {
    }
  }

  /**
   *
   * This method serves as a constructor for the class.
   *
   * @param dt Instantiated DateTime variable
   */
  public DateTime(final DateTime dt) {
    this.cal = Calendar.getInstance();
    try {
      if (!dt.isNull()) {
        this.cal.setTime(dt.getCal().getTime());
      }
    }
    catch (final Exception e) {
    }
  }

  /**
   *
   * This method serves as a constructor for the class.
   *
   * @param year
   * @param month
   * @param day
   */
  public DateTime(final int year, final int month, final int day) {
    this.cal = Calendar.getInstance();
    try {
      this.cal.set(year, month, day);
    }
    catch (final Exception e) {
    }
  }

  /**
   *
   * This method serves as a constructor for the class.
   *
   * @param ms Milliseconds
   */
  public DateTime(final long ms) {
    this.cal = Calendar.getInstance();
    try {
      this.cal.setTimeInMillis(ms);
    }
    catch (final Exception e) {
    }
  }

  public double daysBetween(DateTime dt) {
    double ret = 0.0;

    long diff = TimeUnit.DAYS.convert(dt.getMs(), TimeUnit.MILLISECONDS);

    return ret;
  }

  /**
   *
   * @param value
   * @param sdf
   */
  public DateTime(String value, SimpleDateFormat sdf) {
    try {
      this.build(this, value, sdf, sdf);
    }
    catch (final Exception e) {
    }
  }

  public DateTime(String value, SimpleDateFormat sdf, SimpleDateFormat sdfout) {
    try {
      this.build(this, value, sdf, sdfout);
    }
    catch (final Exception e) {
    }
  }

  /**
   * This method serves as a constructor for the class.
   *
   * @param trim
   * @param string
   */
  public DateTime(String value, String fmt) {
    try {
      final SimpleDateFormat sdfIn = new SimpleDateFormat(fmt);
      this.build(this, value, sdfIn, sdfIn);
    }
    catch (final Exception e) {
    }
  }

  /**
   *
   * net.ajaskey.market.misc.add
   *
   * @param unit DATE|MONTH|YEAR
   * @param knt  Any integer
   */
  public void add(final int unit, final int knt) {
    final DateTime tmp = DateTime.copy(this);
    try {
      if (!this.isNull()) {
        if (unit == DateTime.DATE || unit == DateTime.MONTH || unit == DateTime.YEAR) {
          this.cal.add(unit, knt);
        }
      }
    }
    catch (final Exception e) {
      // set back to previous time
      if (tmp != null) {
        if (!tmp.isNull()) {
          this.cal = tmp.cal;
        }
      }
    }
  }

  /**
   *
   * net.ajaskey.market.misc.format
   *
   * @return
   */
  public String format() {
    String ret = "";
    try {
      ret = this.sdf.format(this.cal.getTime());
    }
    catch (final Exception e) {
      ret = "BAD-DATE-FORMAT";
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.format
   *
   * @param fmt
   * @return
   */
  public String format(final String fmt) {
    String ret = "";
    try {
      final SimpleDateFormat sdf = new SimpleDateFormat(fmt);
      ret = sdf.format(this.cal.getTime());
    }
    catch (final Exception e) {
      ret = "BAD-DATE-FORMAT";
    }
    return ret;
  }

  /**
   * @return the cal
   */
  public Calendar getCal() {
    return this.cal;
  }

  /**
   *
   * net.ajaskey.market.misc.getDay
   *
   * @return
   */
  public String getDay() {
    String ret = "UNKNOWN";
    if (!this.isNull()) {
      ret = this.cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.getDayOfMonth
   *
   * @return
   */
  public int getDayOfMonth() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.DAY_OF_MONTH);
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.getDayOfWeek
   *
   * @return
   */
  public int getDayOfWeek() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.DAY_OF_WEEK);
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.getDayOfYear
   *
   * @return
   */
  public int getDayOfYear() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.DAY_OF_YEAR);
    }
    return ret;
  }

  /**
   *
   * @param dt2
   * @return
   */
  public long getDeltaDays(final DateTime dt2) {
    long dd = 0L;
    try {
      if (!dt2.isNull()) {
        final long s1 = this.getDeltaMilliSeconds(dt2);
        dd = TimeUnit.DAYS.convert(s1, TimeUnit.MILLISECONDS);
      }
    }
    catch (final Exception e) {
      dd = 0L;
    }
    return dd;
  }

  public long getDeltaMilliSeconds(DateTime dt2) {
    long ret = 0L;
    try {
      if (!dt2.isNull()) {
        final long dt1Ms = this.cal.getTimeInMillis();
        final long dt2Ms = dt2.cal.getTimeInMillis();
        ret = dt2Ms - dt1Ms;
      }
    }
    catch (final Exception e) {
      ret = 0L;
    }
    return ret;
  }

  public long getDeltaSeconds(DateTime dt2) {
    long ret = this.getDeltaMilliSeconds(dt2);
    ret *= 1000L;
    return ret;
  }

  /**
   *
   * @param dt2
   * @return
   */
  public double getDeltaYears(final DateTime dt2) {
    final double dd = this.getDeltaDays(dt2);
    return dd / 365.0;
  }

  /**
   *
   * @return
   */
  public int getHour() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.HOUR);
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public int getMinute() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.MINUTE);
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public String getMonth() {
    String ret = "UNKNOWN";
    if (!this.isNull()) {
      ret = this.cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public int getMs() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.MILLISECOND);
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public SimpleDateFormat getSdf() {
    return this.sdf;
  }

  /**
   *
   * @return
   */
  public int getSecond() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.SECOND);
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public Date getTime() {
    Date ret = null;
    if (!this.isNull()) {
      ret = this.cal.getTime();
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public int getYear() {
    int ret = -1;
    if (!this.isNull()) {
      ret = this.cal.get(Calendar.YEAR);
    }
    return ret;
  }

  /**
   *
   * @param dt
   * @return
   */
  public int getYears(DateTime dt) {
    int delta = 0;
    try {
      if (dt.isNull() || this.isNull()) {
        return 0;
      }

      final int y1 = this.cal.get(DateTime.YEAR);
      final int y2 = dt.cal.get(DateTime.YEAR);
      delta = y2 - y1;
      final int m1 = this.cal.get(DateTime.MONTH);
      final int m2 = dt.cal.get(DateTime.MONTH);
      if (m2 < m1) {
        delta--;
      }
      else if (m2 == m1) {
        final int d1 = this.cal.get(DateTime.DATE);
        final int d2 = dt.cal.get(DateTime.DATE);
        if (d2 < d1) {
          delta--;
        }

      }
    }
    catch (final Exception e) {
      delta = 0;
    }
    return delta;
  }

  /**
   *
   * net.ajaskey.market.misc.isEqual
   *
   * @param dt
   * @return
   */
  public boolean isEqual(final DateTime dt) {
    boolean ret = false;
    try {
      if (!dt.isNull()) {
        ret = DateTime.sameDate(dt, this);
      }
    }
    catch (final Exception e) {
      ret = false;
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.isGreaterThan
   *
   * @param dt
   * @return
   */
  public boolean isGreaterThan(final DateTime dt) {

    boolean ret = false;
    try {
      if (!dt.isNull()) {
        ret = dt.cal.before(this.cal);
      }
    }
    catch (final Exception e) {
      ret = false;
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.isGreaterThanOrEqual
   *
   * @param dt
   * @return
   */
  public boolean isGreaterThanOrEqual(final DateTime dt) {
    boolean ret = false;
    try {
      ret = this.isGreaterThan(dt) || this.isEqual(dt);
    }
    catch (final Exception e) {
      ret = false;
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.isLessThan
   *
   * @param dt
   * @return
   */
  public boolean isLessThan(final DateTime dt) {
    boolean ret = false;

    try {
      if (!dt.isNull()) {
        ret = dt.cal.after(this.cal);
      }
    }
    catch (final Exception e) {
      return false;
    }
    return ret;
  }

  /**
   *
   * net.ajaskey.market.misc.isLessThanOrEqual
   *
   * @param dt
   * @return
   */
  public boolean isLessThanOrEqual(final DateTime dt) {
    boolean ret = false;
    try {
      if (!dt.isNull()) {
        ret = this.isLessThan(dt) || this.isEqual(dt);
      }
    }
    catch (final Exception e) {
      return false;
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public boolean isNull() {
    boolean ret = false;
    try {
      if (this.cal == null) {
        ret = true;
      }
    }
    catch (final Exception e) {
      ret = true;
    }
    return ret;
  }

  /**
   *
   * @return
   */
  public boolean isWeekday() {
    boolean ret = false;
    try {
      final int d = this.cal.get(Calendar.DAY_OF_WEEK);
      ret = d > DateTime.SUNDAY && d < DateTime.SATURDAY;
    }
    catch (final Exception e) {
      ret = false;
    }
    return ret;
  }

  /**
   *
   * @param src
   * @return
   */
  public DateTime parse(final String src) {
    DateTime ret = null;
    try {
      if (src != null) {
        final Date d = this.sdf.parse(src);
        ret = new DateTime(d);
      }
    }
    catch (final Exception e) {
      ret = null;
    }
    return ret;
  }

  /**
   *
   * @param src
   * @param fmt
   * @return
   */
  public DateTime parse(final String src, final String fmt) {
    DateTime ret = new DateTime();
    try {
      final SimpleDateFormat sdf = new SimpleDateFormat(fmt);
      final Date d = sdf.parse(src);
      ret = new DateTime(d);
    }
    catch (final Exception e) {
      ret = null;
    }
    return ret;
  }

  /**
   *
   * @param dt
   * @return
   */
  public boolean sameDate(final DateTime dt) {

    if (dt.isNull()) {
      return false;
    }
    try {
      if (this.cal.get(Calendar.YEAR) == dt.cal.get(Calendar.YEAR)) {
        if (this.cal.get(Calendar.MONTH) == dt.cal.get(Calendar.MONTH)) {
          if (this.cal.get(Calendar.DATE) == dt.cal.get(Calendar.DATE)) {
            return true;
          }
        }
      }
    }
    catch (final Exception e) {
      return false;
    }
    return false;
  }

  /**
   *
   * @param c Calendar
   */
  public void set(final Calendar c) {
    if (c != null) {
      if (this.cal == null) {
        this.cal = Calendar.getInstance();
      }
      this.cal.setTime(c.getTime());
    }
  }

  /**
   *
   * @param d Date
   */
  public void set(final Date d) {
    if (d != null) {
      if (this.cal == null) {
        this.cal = Calendar.getInstance();
      }
      this.cal.setTime(d);
    }
  }

  /**
   *
   * @param dt
   */
  public void set(final DateTime dt) {
    if (dt != null) {
      if (this.cal == null) {
        this.cal = Calendar.getInstance();
      }
      this.cal.setTime(dt.cal.getTime());
    }
  }

  public void set(int constant, int value) {
    this.cal.set(constant, value);
  }

  /**
   *
   * @param year
   * @param month
   * @param day
   */
  public void set(final int year, final int month, final int day) {
    if (this.cal == null) {
      this.cal = Calendar.getInstance();
    }
    this.cal.set(year, month, day);
  }

  /**
   *
   */
  public void setFirstWorkDay() {
    final int dow = this.getDayOfWeek();
    if (dow == DateTime.SATURDAY) {
      this.add(DateTime.DATE, 2);
    }
    else if (dow == DateTime.SUNDAY) {
      this.add(DateTime.DATE, 1);
    }
  }

  /**
   *
   * @param simpledateformat
   */
  public void setSdf(final SimpleDateFormat simpledateformat) {
    this.sdf = simpledateformat;
  }

  public void settime(final int hour, final int minute, final int second) {
    this.set(DateTime.HOUR_OF_DAY, hour);
    this.set(DateTime.MINUTE, minute);
    this.set(DateTime.SECOND, second);
  }

  /**
   *
   * @return
   */
  public String toFullString() {
    String ret = "";
    try {
      ret = Utils.sdfFull.format(this.cal.getTime());
    }
    catch (final Exception e) {
      ret = "Invalid Time";
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String ret = "";
    if (this.cal != null) {
      if (this.sdf == null) {
        this.setSdf(Utils.sdf);
      }
      ret = this.sdf.format(this.cal.getTime());
    }
    return ret;
  }

  /**
   *
   * @param retThis
   * @param value
   * @param sdf
   * @param sdfout
   */
  private void build(DateTime retThis, String value, SimpleDateFormat sdf, SimpleDateFormat sdfout) {
    retThis.cal = Calendar.getInstance();
    try {
      final Date d = sdf.parse(value);
      retThis.cal.setTime(d);
      retThis.setSdf(sdfout);
    }
    catch (final Exception e) {
      retThis.cal = null;
    }
  }
}
