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

package net.ajaskey.market.tools.fred.legacy;

import net.ajaskey.common.DateTime;

public class OptumaFileData {

  DateTime date;
  double   val;

  /**
   * This method serves as a constructor for the class.
   *
   */
  public OptumaFileData(final DateTime dt, final double v) {

    this.date = new DateTime(dt);
    this.val = v;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    final String ret = String.format("%s, %.2f", this.date.format("yyyy-MM-dd"), this.val);
    return ret;
  }

}
