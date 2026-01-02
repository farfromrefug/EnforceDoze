package com.akylas.enforcedoze;

import static com.akylas.enforcedoze.Utils.logToLogcat;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


@RequiresApi(api = Build.VERSION_CODES.N)
public class AirplaneTileService extends TileService {

    private static final String TAG = "AirplaneTileService";
    private static final int TILE_UPDATE_DELAY_MS = 150;  // Delay to ensure tile updates properly
    private SharedPreferences settings;
    private boolean airplaneModeEnabled;

    private static void log(String message) {
        logToLogcat(TAG, message);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        log("Airplane QuickTile added");
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        airplaneModeEnabled = settings.getBoolean("turnOnAirplaneInDoze", false);
        updateTileState(airplaneModeEnabled);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        log("Airplane QuickTile removed");
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        log("Airplane QuickTile onStartListening");
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        airplaneModeEnabled = settings.getBoolean("turnOnAirplaneInDoze", false);
        updateTileState(airplaneModeEnabled);
    }


    @Override
    public void onClick() {
        super.onClick();
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        airplaneModeEnabled = settings.getBoolean("turnOnAirplaneInDoze", false);
        
        // Toggle the setting
        boolean newValue = !airplaneModeEnabled;
        
        log(String.format("%s airplane mode in Doze", newValue ? "Enabling" : "Disabling"));
        
        // Save the new value
        settings.edit().putBoolean("turnOnAirplaneInDoze", newValue).apply();
        
        // Update the tile
        updateTileState(newValue);
        
        // Notify the service to reload settings
        sendBroadcastToReloadSettings();
    }

    private void sendBroadcastToReloadSettings() {
        android.content.Intent intent = new android.content.Intent("reload-settings");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateTileState(final boolean active) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Tile tile = getQsTile();
                if (tile != null) {
                    tile.setLabel(getString(R.string.airplane_tile_label));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        tile.setSubtitle(active ? getString(R.string.tile_subtitle_on) : getString(R.string.tile_subtitle_off));
                    }
                    tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
                    tile.updateTile();
                }
            }
        }, TILE_UPDATE_DELAY_MS);
    }
}
