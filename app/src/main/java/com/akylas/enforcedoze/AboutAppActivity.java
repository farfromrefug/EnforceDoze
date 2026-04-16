package com.akylas.enforcedoze;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        CustomTabs.with(getApplicationContext()).warm();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView textVersion = findViewById(R.id.textVersion);
        String versionName = BuildConfig.VERSION_NAME;
        String buildNumber = String.valueOf(BuildConfig.VERSION_CODE);
        textVersion.setText(getString(R.string.version_format, versionName, buildNumber));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showLicences(View v) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.licenses));
        builder.setMessage(getString(R.string.licenses_text));
        builder.setPositiveButton(getString(R.string.okay_button_text), (dialog, i) -> dialog.dismiss());
        builder.show();
    }

    public void openSourceCode(View v) {
        openUrl("https://github.com/farfromrefug/EnforceDoze");
    }

    public void openGithubSponsors(View v) {
        openUrl("https://github.com/sponsors/farfromrefug");
    }

    public void openLiberapay(View v) {
        openUrl("https://liberapay.com/farfromrefuge");
    }

    public void showTranslationCreditsDialog(View v) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(getString(R.string.translation_credits_dialog_title));
        builder.setMessage(getString(R.string.translation_credits_dialog_text));
        builder.setPositiveButton(getString(R.string.okay_button_text), (dialog, i) -> dialog.dismiss());
        builder.show();
    }

    public void showPrivacyPolicy(View v) {
        openUrl("https://www.akylas.fr/privacy");
    }

    private void openUrl(String url) {
        CustomTabs.with(getApplicationContext())
                .setStyle(new CustomTabs.Style(getApplicationContext())
                        .setShowTitle(true)
                        .setExitAnimation(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .setToolbarColor(R.color.colorPrimary))
                .openUrl(url, this);
    }
}
