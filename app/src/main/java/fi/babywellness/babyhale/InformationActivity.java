package fi.babywellness.babyhale;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class InformationActivity extends BaseActivity {

    public static final String TYPE_KEY = "TYPE";
    public static final int SHOW_ALCOHOL = 42;
    public static final int SHOW_TOBACCO = 7777;
    public static final int SHOW_DRUGS = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> speechText = new ArrayList<>();
        int type = getIntent().getIntExtra(TYPE_KEY, 0);
        switch (type) {
            case SHOW_ALCOHOL:
                setContentView(R.layout.layout_alcohol_info);
                setUpToolbar();
                setToolbarTitle(R.string.alcohol);
                speechText.add(getString(R.string.alcoholInfoHeader1));
                speechText.addAll(getTextSplitDotsAndNewlines(getString(R.string.alcoholInfoText1)));
                speechText.add(getString(R.string.alcoholInfoHeader2));
                speechText.addAll(getTextSplitDotsAndNewlines(getString(R.string.alcoholInfoText2)));
                break;
            case SHOW_TOBACCO:
                setContentView(R.layout.layout_tobacco_info);
                setUpToolbar();
                setToolbarTitle(R.string.tobacco);
                speechText.add(getString(R.string.tobaccoInfoHeader1));
                speechText.addAll(getTextSplitDotsAndNewlines(getString(R.string.tobaccoInfoText1)));
                speechText.add(getString(R.string.tobaccoInfoHeader2));
                speechText.addAll(getTextSplitDotsAndNewlines(getString(R.string.tobaccoInfoText2)));
                break;
            case SHOW_DRUGS:
                setContentView(R.layout.layout_drugs_info);
                setUpToolbar();
                setToolbarTitle(R.string.drugs);
                speechText.add(getString(R.string.drugsInfoHeader1));
                speechText.addAll(getTextSplitDotsAndNewlines(getString(R.string.drugsInfoText1)));
                speechText.add(getString(R.string.drugsInfoHeader2));
                speechText.addAll(getTextSplitDotsAndNewlines(getString(R.string.drugsInfoText2)));
                break;
        }
        setSpeechText(speechText);
    }
}
