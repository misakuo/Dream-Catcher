/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package me.moxun.dreamcatcher.connector.util;

public interface LogInterface {

    void setLoggable(boolean loggable);

    public void v(String msg);
    public void v(String tag, String msg);
    public void v(String tag, Throwable tr);
    public void v(String tag, String msg, Throwable tr);

    public void d(String msg);
    public void d(String tag, String msg);
    public void d(String tag, Throwable tr);
    public void d(String tag, String msg, Throwable tr);

    public void i(String msg);
    public void i(String tag, String msg);
    public void i(String tag, Throwable tr);
    public void i(String tag, String msg, Throwable tr);

    public void w(String msg);
    public void w(String tag, String msg);
    public void w(String tag, Throwable tr);
    public void w(String tag, String msg, Throwable tr);

    public void e(String msg);
    public void e(String tag, String msg);
    public void e(String tag, Throwable tr);
    public void e(String tag, String msg, Throwable tr);

}
