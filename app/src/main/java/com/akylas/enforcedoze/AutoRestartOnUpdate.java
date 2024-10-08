package com.akylas.enforcedoze;

import static com.akylas.enforcedoze.Utils.logToLogcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class AutoRestartOnUpdate extends BroadcastReceiver {
    public static String TAG = "EnforceDoze";

    private static void log(String message) {
        logToLogcat(TAG, message);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && intent.getDataString().contains(context.getPackageName())) {
            log("Application updated, restarting service if enabled");
            boolean isServiceEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("serviceEnabled", false);
            if (isServiceEnabled) {
                if (Utils.isMyServiceRunning(ForceDozeService.class, context)) {
                    context.stopService(new Intent(context, ForceDozeService.class));
                    context.startService(new Intent(context, ForceDozeService.class));
                } else if (!Utils.isMyServiceRunning(ForceDozeService.class, context)) {
                    context.startService(new Intent(context, ForceDozeService.class));
                }
            } else {
                log("Service not enabled, skip restarting");
            }
        }
    }
}
