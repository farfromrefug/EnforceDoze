package com.akylas.enforcedoze;


import static com.akylas.enforcedoze.Utils.logToLogcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class BootCompleteReceiver extends BroadcastReceiver {
    public static String TAG = "EnforceDoze";
    private static void log(String message) {
        logToLogcat(TAG, message);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isServiceEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("serviceEnabled", false);
        log("Received BOOT_COMPLETED intent, isServiceEnabled=" + Boolean.toString(isServiceEnabled));
        if (isServiceEnabled) {
            Intent startServiceIntent = new Intent(context, ForceDozeService.class);
            context.startService(startServiceIntent);
        }
    }
}
