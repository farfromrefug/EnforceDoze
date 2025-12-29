package com.akylas.enforcedoze;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.provider.Settings;

public class RequestIgnoreBatteryActivity extends AppCompatActivity {
    public static final String ACTION_IGNORE_RESULT = "com.akylas.enforcedoze.ACTION_IGNORE_BATTERY_OPTIMIZATION_RESULT";
    public static final String EXTRA_IGNORED = "com.akylas.enforcedoze.EXTRA_IGNORED";

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    boolean ignored = false;
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    if (pm != null) {
                        ignored = pm.isIgnoringBatteryOptimizations(getPackageName());
                    }
                    Intent broadcast = new Intent(ACTION_IGNORE_RESULT).putExtra(EXTRA_IGNORED, ignored);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
                    finish();
                });

        Intent requestIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        requestIntent.setData(Uri.parse("package:" + getPackageName()));
        // launch the system dialog; result callback will run when user returns
        launcher.launch(requestIntent);
    }
}
