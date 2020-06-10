package com.cxh.task.util;

import android.util.Log;

public class LogUtil {
    private static final String LOG_TAG = "LogUtil";

    public static void iniLogger() {
    }

    /**
     * 是否允许打印日志
     */
    private static boolean isLoggerAble() {
        return true;
    }

    public static void d(String tag, String message) {
        if (isLoggerAble()) {
            Log.d(LOG_TAG, tag + message);
        }
    }

    // log.d
    public static void d(String message) {
        if (isLoggerAble()) {
            Log.d(LOG_TAG, message);
        }
    }

    // log.w
    public static void w(String message) {
        if (isLoggerAble()) {
            Log.w(LOG_TAG, message);
        }
    }

    // log.e
    public static void e(String message) {
        if (isLoggerAble()) {
            Log.e(LOG_TAG, message);
        }

    }

    // log.e
    public static void e(String tag, String message) {
        if (isLoggerAble()) {
            Log.e(LOG_TAG, tag + " " + message);
        }

    }

    // log.e
    public static void e(String tag, Exception message) {
        if (isLoggerAble()) {
            Log.e(LOG_TAG, tag + " " + message.toString());
        }

    }

    // log.e
    public static void e(Throwable e, String message) {
        if (isLoggerAble()) {
            Log.e(LOG_TAG, message);
        }

    }

    // log.i
    public static void i(String message) {
        if (isLoggerAble()) {
            Log.i(LOG_TAG, message);
        }
    }
}
