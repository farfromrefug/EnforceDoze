package com.akylas.enforcedoze;

import static com.akylas.enforcedoze.Utils.logToLogcat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class DozeTunablesActivity extends AppCompatActivity {

    public static String TAG = "EnforceDoze";
    public static boolean suAvailable = false;
    private static final String PREF_HIDE_CATEGORY_LABELS = "hidePreferenceCategoryLabels";


    private static void log(String message) {
        logToLogcat(TAG, message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunables);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new DozeTunablesFragment())
                    .commit();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (!suAvailable) {
            menu.getItem(0).setVisible(false);
        }

        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.doze_tunables_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_apply_tunables) {
            applyTunables();
        } else if (id == R.id.action_copy_tunables) {
            showCopyTunableDialog();
        } else
            if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void applyTunables() {
        String tunable_string = DozeTunableHandler.getInstance().getTunableString();
        log("Setting device_idle_constants=" + tunable_string);
        executeCommand("settings put global device_idle_constants " + tunable_string);
        Toast.makeText(this, getString(R.string.applied_success_text), Toast.LENGTH_SHORT).show();
    }

    public void showCopyTunableDialog() {
        String tunable_string = DozeTunableHandler.getInstance().getTunableString();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.adb_command_text));
        builder.setMessage("You can apply the new values using ADB by running the following command:\n\nadb shell settings put global device_idle_constants " + tunable_string);
        builder.setPositiveButton(getString(R.string.close_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.copy_to_clipboard_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Tunable k/v string", "adb shell settings put global device_idle_constants " + tunable_string);
                clipboard.setPrimaryClip(clip);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public void executeCommand(final String command) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<String> output = Shell.SU.run(command);
                if (output != null) {
                    printShellOutput(output);
                } else {
                    log("Error occurred while executing command (" + command + ")");
                }
            }
        });
    }

    public void printShellOutput(List<String> output) {
        if (!output.isEmpty()) {
            for (String s : output) {
                log(s);
            }
        }
    }

    @SuppressLint("ValidFragment")
    public  static class DozeTunablesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        MaterialDialog grantPermProgDialog;
        boolean isSuAvailable = false;

        private void removeIconSpace(PreferenceGroup group) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                Preference pref = group.getPreference(i);
                pref.setIconSpaceReserved(false);

                if (pref instanceof PreferenceGroup) {
                    removeIconSpace((PreferenceGroup) pref);
                }
            }
        }

        private void updateCategoryLabelsVisibility(PreferenceGroup group, boolean hide) {
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                Preference pref = group.getPreference(i);
                if (pref instanceof PreferenceCategory) {
                    PreferenceCategory category = (PreferenceCategory) pref;
                    if (hide) {
                        // Save the original title in the tag if not already saved
                        if (category.getExtras().getCharSequence("originalTitle") == null && category.getTitle() != null) {
                            category.getExtras().putCharSequence("originalTitle", category.getTitle());
                        }
                        category.setTitle("");
                    } else {
                        // Restore the original title from the tag
                        CharSequence originalTitle = category.getExtras().getCharSequence("originalTitle");
                        if (originalTitle != null) {
                            category.setTitle(originalTitle);
                        }
                    }
                }
                if (pref instanceof PreferenceGroup) {
                    updateCategoryLabelsVisibility((PreferenceGroup) pref, hide);
                }
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                RecyclerView recyclerView =
                        v.findViewById(androidx.preference.R.id.recycler_view);

                if (recyclerView != null) {
                    int bottomInset = insets
                            .getInsets(WindowInsetsCompat.Type.systemBars())
                            .bottom;

                    recyclerView.setPadding(
                            recyclerView.getPaddingLeft(),
                            recyclerView.getPaddingTop(),
                            recyclerView.getPaddingRight(),
                            bottomInset
                    );
                    recyclerView.setClipToPadding(false);
                }
                return insets;
            });
        }

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            addPreferencesFromResource(R.xml.prefs_doze_tunables);
            removeIconSpace(getPreferenceScreen());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("tunablesPreferenceScreen");
            PreferenceCategory lightDozeSettings = (PreferenceCategory) findPreference("lightDozeSettings");

            if (!Utils.isDeviceRunningOnN()) {
                preferenceScreen.removePreference(lightDozeSettings);
            }

            if (!preferences.getBoolean("isSuAvailable", false)) {
                grantPermProgDialog = new MaterialDialog.Builder(getActivity())
                        .title(getString(R.string.please_wait_text))
                        .cancelable(false)
                        .autoDismiss(false)
                        .content(getString(R.string.requesting_su_access_text))
                        .progress(true, 0)
                        .show();
                log("Check if SU is available, and request SU permission if it is");
                Tasks.executeInBackground(getActivity(), new BackgroundWork<Boolean>() {
                    @Override
                    public Boolean doInBackground() throws Exception {
                        return Shell.SU.available();
                    }
                }, new Completion<Boolean>() {
                    @Override
                    public void onSuccess(Context context, Boolean result) {
                        if (grantPermProgDialog != null) {
                            grantPermProgDialog.dismiss();
                        }
                        isSuAvailable = result;
                        suAvailable = isSuAvailable;
                        log("SU available: " + Boolean.toString(result));
                        if (isSuAvailable) {
                            log("Phone is rooted and SU permission granted");
                            if (!Utils.isSecureSettingsPermissionGranted(getActivity())) {
                                executeCommand("pm grant com.akylas.enforcedoze android.permission.WRITE_SECURE_SETTINGS");
                            }
                        } else {
                            log("SU permission denied or not available");
                            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                            builder.setTitle(getString(R.string.error_text));
                            builder.setMessage(getString(R.string.tunables_su_not_available_error_text));
                            builder.setPositiveButton(getString(R.string.okay_button_text), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }

                    @Override
                    public void onError(Context context, Exception e) {
                        Log.e(TAG, "Error querying SU: " + e.getMessage());
                    }
                });
            } else {
                suAvailable = true;
            }

            // Apply initial category label visibility
            boolean hideCategoryLabels = preferences.getBoolean(PREF_HIDE_CATEGORY_LABELS, false);
            updateCategoryLabelsVisibility(getPreferenceScreen(), hideCategoryLabels);
            
            // Register preference change listener
            preferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            // Unregister preference change listener
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (preferences != null) {
                preferences.unregisterOnSharedPreferenceChangeListener(this);
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            // Handle hide category labels preference change
            if (PREF_HIDE_CATEGORY_LABELS.equals(key)) {
                boolean hideCategoryLabels = sharedPreferences.getBoolean(PREF_HIDE_CATEGORY_LABELS, false);
                updateCategoryLabelsVisibility(getPreferenceScreen(), hideCategoryLabels);
            }
        }


        public void executeCommand(final String command) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<String> output = Shell.SU.run(command);
                    if (output != null) {
                        printShellOutput(output);
                    } else {
                        log("Error occurred while executing command (" + command + ")");
                    }
                }
            });
        }

        public void printShellOutput(List<String> output) {
            if (!output.isEmpty()) {
                for (String s : output) {
                    log(s);
                }
            }
        }
    }
}
