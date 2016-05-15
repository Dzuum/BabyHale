package fi.babywellness.babyhale;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    private static String PREFS_NAME = "Baby_Hale_Preferences";
    private static String ASK_TTS_INSTALL_KEY = "Ask_to_install";

    private static long PAUSE_TIME = 250;
    private static TextToSpeech textToSpeech = null;
    private static boolean hasTriedInit = false;
    private static boolean isTTSInitialised = false;
    private static boolean isSpeaking = false;

    private static NetworkStateReceiver networkStateReceiver  = null;
    private static IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    private TextView titleText;
    private boolean enableMenu;
    private List<String> speechTextList;

//    private AlertDialog.Builder dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableMenu = true;
        speechTextList = new ArrayList<>();

        HTTPRequests.initQueueIfNeeded(this);

        if (networkStateReceiver == null)
            networkStateReceiver = new NetworkStateReceiver();

        initTTS();
    }

    protected void setUpToolbar() {
        this.setUpToolbar(true);
    }

    protected void setUpToolbar(boolean enableUp) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        titleText = (TextView) toolbar.findViewById(R.id.toolbar_title);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle("");

            if (enableUp)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void disableMenu() {
        enableMenu = false;
        invalidateOptionsMenu();
    }

    protected void enableMenu() {
        enableMenu = true;
        invalidateOptionsMenu();
    }

    protected void setToolbarTitle(int resId) {
        titleText.setText(resId);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (enableMenu) {
            if (isSpeaking) {
                menu.findItem(R.id.menu_text_to_speech).setIcon(R.drawable.ic_stop_black_48dp);
            } else {
                menu.findItem(R.id.menu_text_to_speech).setIcon(R.drawable.ic_hearing_black_48dp);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (enableMenu) {
            getMenuInflater().inflate(R.menu.menu_primary, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.menu_text_to_speech) {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            } else {
                if (textToSpeech.isSpeaking())
                    stopSpeech();
                else
                    speakText();

                supportInvalidateOptionsMenu();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        stopSpeech();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        stopSpeech();
        supportInvalidateOptionsMenu();
        getApplication().unregisterReceiver(networkStateReceiver);

//        if (dialog != null)
//            dialog.

        super.onPause();
    }

    @Override
    protected void onResume() {
        getApplication().registerReceiver(networkStateReceiver, intentFilter);

        //This needs to be here for the menu updates to work in other Activities too beside the launching one (MainActivity)
        if (textToSpeech != null && isTTSInitialised) {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId.equals("end")) {
                        stopSpeech();
                        supportInvalidateOptionsMenu();
                    }
                }

                @Override
                public void onError(String utteranceId) {
                    stopSpeech();
                    supportInvalidateOptionsMenu();
                }

                @Override
                public void onError(String utteranceId, int errorCode) {
                    stopSpeech();
                    supportInvalidateOptionsMenu();
                }
            });
        }

        super.onResume();
    }

    private void initTTS() {
        final SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean askToInstall = preferences.getBoolean(ASK_TTS_INSTALL_KEY, true);

        if (textToSpeech == null && !hasTriedInit && askToInstall) {
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int availability = textToSpeech.isLanguageAvailable(Locale.getDefault());

                        if (availability == TextToSpeech.LANG_MISSING_DATA || availability == TextToSpeech.LANG_NOT_SUPPORTED) {
                            promptToInstallTTSData(preferences);
                            isTTSInitialised = false;
                        } else {
                            textToSpeech.setLanguage(Locale.getDefault());
                            hasTriedInit = true;
                            isTTSInitialised = true;
                        }
                    } else {
                        isTTSInitialised = false;
                        //TODO
                        //hasTriedInit?
                    }
                }
            });
        } else {
            stopSpeech();
        }
    }

    private void promptToInstallTTSData(final SharedPreferences preferences) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.tts_data_title);
        dialog.setMessage(R.string.tts_data_message);
        dialog.setPositiveButton(R.string.tts_data_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        hasTriedInit = true;
                        Intent installIntent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(installIntent);
                    }
                });
        dialog.setNegativeButton(R.string.tts_data_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        hasTriedInit = true;
                    }
                });
        dialog.setNeutralButton(R.string.tts_data_no_remember, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        hasTriedInit = true;
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean(ASK_TTS_INSTALL_KEY, false);
                        editor.apply();

                        showRememberDialog();
                    }
                });
        dialog.show();
    }

    private void showRememberDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.tts_data_remember_title);
        dialog.setMessage(R.string.tts_data_remember_message);
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    protected void shutdownTTS() {
        if (textToSpeech != null && isTTSInitialised)
            textToSpeech.shutdown();

        textToSpeech = null;
    }

    protected void stopSpeech() {
        if (textToSpeech != null && isTTSInitialised)
            textToSpeech.stop();

        isSpeaking = false;
    }

    protected void speakText() {
        if (textToSpeech == null || !isTTSInitialised ||
            speechTextList == null || speechTextList.size() == 0)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakAtLeastAPI21();
        } else {
            speakUnderAPI21();
        }

        isSpeaking = true;
    }

    @SuppressWarnings("deprecation")
    private void speakUnderAPI21() {
        HashMap<String, String> params = new HashMap<>();

        for (int i = 0; i < speechTextList.size(); i++) {
            params.clear();

            if (i == speechTextList.size() - 1) {
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "end");
                textToSpeech.speak(speechTextList.get(i), TextToSpeech.QUEUE_ADD, params);
            } else if (i == 0) {
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id" + i);
                textToSpeech.speak(speechTextList.get(i), TextToSpeech.QUEUE_FLUSH, params);
                textToSpeech.playSilence(PAUSE_TIME, TextToSpeech.QUEUE_ADD, params);
            } else {
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id" + i);
                textToSpeech.speak(speechTextList.get(i), TextToSpeech.QUEUE_ADD, params);
                textToSpeech.playSilence(PAUSE_TIME, TextToSpeech.QUEUE_ADD, params);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void speakAtLeastAPI21() {
        Bundle params = new Bundle();

        for (int i = 0; i < speechTextList.size(); i++) {
            params.clear();

            if (i == speechTextList.size() - 1) {
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "end");
                textToSpeech.speak(speechTextList.get(i), TextToSpeech.QUEUE_ADD, params, "end");
            } else if (i == 0) {
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id" + i);
                textToSpeech.speak(speechTextList.get(i), TextToSpeech.QUEUE_FLUSH, params, "id" + i);
                textToSpeech.playSilentUtterance(PAUSE_TIME, TextToSpeech.QUEUE_ADD, "id" + i);
            } else {
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id" + i);
                textToSpeech.speak(speechTextList.get(i), TextToSpeech.QUEUE_ADD, params, "id" + i);
                textToSpeech.playSilentUtterance(PAUSE_TIME, TextToSpeech.QUEUE_ADD, "id" + i);
            }
        }
    }

    protected void setSpeechText(List<String> textList) {
        this.speechTextList = textList;
    }

    protected List<String> getTextSplitDotsAndNewlines(String text) {
        List<String> splitText = new LinkedList<>(Arrays.asList(text.split("[\n.]")));

        for (int i = 0; i < splitText.size(); i++) {
            if (splitText.get(i).trim().isEmpty()) {
                splitText.remove(i);
                i--;
            }
        }

        return splitText;
    }
}
