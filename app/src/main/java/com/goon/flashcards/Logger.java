package com.goon.flashcards;

import android.util.Log;

public class Logger {

    // We will set this to the class name.
    // Pull the local TAG to use in logs.

    public static void logger(String tag, String message)
    {
        Log.v(tag, message);
    }

    public static void elogger(String tag, String message)
    {
        // Get calling function.
        // We don't want to get stacktrace 0, because that's this function.
        // Need 1.
        String method_name = new Throwable()
                .getStackTrace()[1]
                .getMethodName();

        Log.e(tag, "Error in " + method_name + ": " + message);
    }

}
