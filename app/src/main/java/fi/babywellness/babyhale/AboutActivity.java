package fi.babywellness.babyhale;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends BaseActivity {

    AlertDialog licenseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setUpToolbar();
        setToolbarTitle(R.string.title_activity_about);

        List<String> speechText = new ArrayList<>();
        speechText.add(getString(R.string.about_title));
        speechText.add(getString(R.string.about_client_title));
        speechText.add(getString(R.string.baby_wellness));
        speechText.add(getString(R.string.about_developers_title));
        speechText.add(getString(R.string.about_developers));
        speechText.add(getString(R.string.copyright));
        setSpeechText(speechText);

        Button licenseButton = (Button) findViewById(R.id.button_licenses);
        licenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayLicensesDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean success = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_about).setVisible(false);
        return success;
    }

    @Override
    protected void onPause() {
        if (licenseDialog != null)
            licenseDialog.dismiss();

        super.onPause();
    }

    private void displayLicensesDialog() {
        WebView view = (WebView) LayoutInflater.from(this).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");
        licenseDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about_licenses_title))
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
