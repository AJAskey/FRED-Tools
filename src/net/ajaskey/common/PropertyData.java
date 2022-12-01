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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyData {

  private final static double errVal = -123454321.0;

  public static boolean isErr(double val) {
    return val == PropertyData.errVal;
  }

  public static boolean isErr(int val) {
    return val == (int) PropertyData.errVal;
  }

  public static boolean isErr(long val) {
    return val == (long) PropertyData.errVal;
  }

  /**
   *
   * @param args
   */
  public static void main(String[] args) {

    final PropertyData oprop = new PropertyData("D:\\dev\\eclipse-workspace\\Market\\option.properties");

    final String code = oprop.getPropertyS("price.code");
    final Double ulBuy = oprop.getPropertyD("price.ulBuy");
    final Double ulSell = oprop.getPropertyD("price.ulSell");
    final Integer holdDays = oprop.getPropertyI("price.hold");

    System.out.println(code);
    System.out.println(ulBuy);
    System.out.println(ulSell);
    System.out.println(holdDays);

  }

  private final Properties prop = new Properties();

  private String propFile = "";

  /**
   *
   * @param filePathAndName
   */
  public PropertyData(String filePathAndName) {
    this.propFile = filePathAndName;
    this.readProperties();

  }

  /**
   *
   * @param key
   * @return
   */
  public Boolean getPropertyB(String key) {

    Boolean ret = false;
    try {
      final String value = this.prop.getProperty(key);
      ret = Boolean.parseBoolean(value);
    }
    catch (final Exception e) {
      ret = false;
      e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   * @param key
   * @return
   */
  public Double getPropertyD(String key) {

    Double ret = 0.0;
    try {
      final String value = this.prop.getProperty(key);
      ret = Double.parseDouble(value);
    }
    catch (final Exception e) {
      ret = PropertyData.errVal;
      // e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   * @param key
   * @return
   */
  public Integer getPropertyI(String key) {

    Integer ret = 0;
    try {
      final String value = this.prop.getProperty(key);
      ret = Integer.parseInt(value);
    }
    catch (final Exception e) {
      ret = (int) PropertyData.errVal;
      e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   * @param key
   * @return
   */
  public Long getPropertyL(String key) {

    Long ret = 0L;
    try {
      final String value = this.prop.getProperty(key);
      ret = Long.parseLong(value);
    }
    catch (final Exception e) {
      ret = (long) PropertyData.errVal;
      e.printStackTrace();
    }
    return ret;
  }

  /**
   *
   * @param key
   * @return
   */
  public String getPropertyS(String key) {

    return this.prop.getProperty(key);
  }

  /**
   *
   */
  private void readProperties() {
    try (InputStream input = new FileInputStream(this.propFile)) {

      this.prop.load(input);

    }
    catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

}
