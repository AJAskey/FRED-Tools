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

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Debug {

  public static final Level INFO = Level.INFO;

  public static final Level SEVERE  = Level.SEVERE;
  public static final Level WARNING = Level.WARNING;

  private static boolean isInit = false;

  public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  /**
   *
   * net.ajaskey.market.tools.fred.init
   *
   * @param logfileName
   */
  public static void init(final String logfileName) {

    if (Debug.isInit) {
      return;
    }

    Handler fileHandler = null;
    Formatter simpleFormatter = null;

    try {

      LogManager.getLogManager().reset();

      fileHandler = new FileHandler(logfileName);

      simpleFormatter = new SimpleFormatter();
      fileHandler.setFormatter(simpleFormatter);

      fileHandler.setLevel(Level.ALL);

      Debug.LOGGER.addHandler(fileHandler);

      Debug.isInit = true;

    }
    catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
