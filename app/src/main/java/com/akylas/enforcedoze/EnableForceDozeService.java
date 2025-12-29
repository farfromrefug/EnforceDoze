package com.akylas.enforcedoze;

import static com.akylas.enforcedoze.Utils.logToLogcat;
import static com.akylas.enforcedoze.Utils.startForceDozeService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class EnableForceDozeService extends BroadcastReceiver {
    public static String TAG = "EnforceDoze";
    private static void log(String message) {
        logToLogcat(TAG, message);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        log("com.akylas.enforcedoze.ENABLE_FORCEDOZE broadcast intent received started: ");
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("serviceEnabled", true).apply();
        if (!Utils.isMyServiceRunning(ForceDozeService.class, context)) {
            startForceDozeService(context);
//            context.startService(new Intent(context, ForceDozeService.class));
        }
        // Hide disabled notification
        Utils.hideDisabledNotification(context);
    }
}
