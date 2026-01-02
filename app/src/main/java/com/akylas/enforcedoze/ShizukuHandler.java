package com.akylas.enforcedoze;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

public class ShizukuHandler {
    private static final String TAG = "ShizukuHandler";
    private static ShizukuHandler instance;
    private Context context;
    private boolean isShizukuAvailable = false;
    private OnAvailibilityChange onAvailibilityChangeListener;

    interface OnAvailibilityChange {
        public void onChange(Boolean value);
    }


    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER =
            (requestCode, grantResult) -> {
                boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
                Log.i(TAG, "Shizuku permission result: " + granted);
                isShizukuAvailable = granted;
                if (onAvailibilityChangeListener != null) {
                    onAvailibilityChangeListener.onChange(isShizukuAvailable);
                }
            };

    private ShizukuHandler(Context context) {
        this.context = context.getApplicationContext();
        checkShizukuAvailability();
    }

    public static synchronized ShizukuHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ShizukuHandler(context);
        }
        return instance;
    }

    public void setOnAvailibilityChangeListener(OnAvailibilityChange onAvailibilityChangeListener) {
        this.onAvailibilityChangeListener = onAvailibilityChangeListener;
    }

    public void checkShizukuAvailability() {
        try {
            isShizukuAvailable = Shizuku.pingBinder();
            if (isShizukuAvailable) {
                if (Shizuku.isPreV11()) {
                    // Pre-v11 is not supported
                    isShizukuAvailable = false;
                    Log.w(TAG, "Shizuku pre-v11 is not supported");
                } else {
                    if (checkShizukuPermission() != PackageManager.PERMISSION_GRANTED) {
                        isShizukuAvailable = false;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking Shizuku availability: " + e.getMessage());
            isShizukuAvailable = false;
        }
    }

    public boolean isShizukuAvailable() {
        return isShizukuAvailable;
    }

    public int checkShizukuPermission() {
        if (Shizuku.isPreV11()) {
            return PackageManager.PERMISSION_DENIED;
        }
        return Shizuku.checkSelfPermission();
    }

    public void requestShizukuPermission() {
        if (Shizuku.isPreV11()) {
            Log.w(TAG, "Shizuku pre-v11 does not support runtime permission");
            return;
        }
        
        if (checkShizukuPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
            Shizuku.requestPermission(0);
        }
    }

    public void removePermissionResultListener() {
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    /**
     * Execute a shell command using Shizuku
     * @param command The command to execute
     * @param callback Callback to receive the output
     */
    public void executeCommand(@NonNull String command, @NonNull OnCommandResultListener callback) {
        executeCommand(command, callback, false);
    }

    Method shizukuNewProcessMethod = null;
    /**
     * Execute a shell command using Shizuku
     * @param command The command to execute
     * @param callback Callback to receive the output
     * @param printOutput Whether to print the output to logs
     */
    public void executeCommand(@NonNull String command, @NonNull OnCommandResultListener callback, boolean printOutput) {
        new Thread(() -> {
            List<String> stdout = new ArrayList<>();
            List<String> stderr = new ArrayList<>();
            int exitCode = -1;

            try {
                if (!isShizukuAvailable) {
                    Log.e(TAG, "Shizuku is not available");
                    callback.onCommandResult(0, -1, stdout, stderr);
                    return;
                }

                if (checkShizukuPermission() != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Shizuku permission not granted");
                    callback.onCommandResult(0, -1, stdout, stderr);
                    return;
                }
                if (shizukuNewProcessMethod == null) {
                    Class<?> clazz = Class.forName("rikka.shizuku.Shizuku");
                    shizukuNewProcessMethod = clazz.getDeclaredMethod("newProcess", String[].class,String[].class, String.class);
                    shizukuNewProcessMethod.setAccessible(true);
                }
                String[] cmd = new String[] { "sh", "-c", command };
                Object[] invokeArgs = new Object[] { cmd, null, null };

                ShizukuRemoteProcess process = (ShizukuRemoteProcess) shizukuNewProcessMethod.invoke(null, invokeArgs);
//                ShizukuRemoteProcess process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, null);

                // Read stdout
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stdout.add(line);
                        if (printOutput) {
                            Log.i(TAG, line);
                        }
                    }
                }

                // Read stderr
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stderr.add(line);
                        if (printOutput) {
                            Log.e(TAG, line);
                        }
                    }
                }

                exitCode = process.waitFor();
                process.destroy();

            } catch (Exception e) {
                Log.e(TAG, "Error executing command: " + e.getMessage());
                e.printStackTrace();
            }

            callback.onCommandResult(0, exitCode, stdout, stderr);
        }).start();
    }

    /**
     * Callback interface for command execution results
     */
    public interface OnCommandResultListener {
        void onCommandResult(int commandCode, int exitCode, List<String> stdout, List<String> stderr);
    }
}
