package fi.babywellness.babyhale;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends BaseActivity implements HTTPRequests.HTTPRequestListener, UpdatableTTS {

    public static final String VNR_SEARCH = "vnr";
    public static final String NAME_SEARCH = "name";
    public static final String SHOW_FULL = "showFull";

    private String titleText;
    private String fullContentText;
    private String parsedContentText;
    private String[] foundNames;
    private String vnr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            Log.d("WAT", "LOAD INSTANCE STATE");
            titleText = savedInstanceState.getString("titleText");
            fullContentText = savedInstanceState.getString("fullContentText");
            parsedContentText = savedInstanceState.getString("parsedContentText");
            foundNames = savedInstanceState.getStringArray("foundNames");
            vnr = savedInstanceState.getString("vnr");
        }

        setContentView(R.layout.layout_product_activity);
        setUpToolbar();
        setToolbarTitle(R.string.title_product_activity);

        changeFragment(new LoadingScreenFragment(), null, false);

        if (getIntent().hasExtra(VNR_SEARCH)) {
            vnr = getIntent().getStringExtra(VNR_SEARCH);
            HTTPRequests.requestWithVnr(this, vnr);
        } else if (getIntent().hasExtra(NAME_SEARCH)) {
            HTTPRequests.requestWithName(this, getIntent().getStringExtra(NAME_SEARCH));
        }
    }

    @Override
    protected void onPause() {
        HTTPRequests.cancelAll();
        super.onPause();
    }

    public void onFoundSingle(String title, String contentText, boolean addToBackStack) {
        titleText = title;
        fullContentText = contentText;
        parsedContentText = parseContentText(contentText);

        fullContentText = fixHTMLLists(fullContentText);
        parsedContentText = fixHTMLLists(parsedContentText);

        Bundle bundle = new Bundle();
        bundle.putString(ProductInformationFragment.TITLE_KEY, title);
        bundle.putString(ProductInformationFragment.VNR_KEY, vnr);
        bundle.putString(ProductInformationFragment.FULL_CONTENT_TEXT_KEY, fullContentText);
        bundle.putString(ProductInformationFragment.PARSED_CONTENT_TEXT_KEY, parsedContentText);

        changeFragment(new ProductInformationFragment(), bundle, addToBackStack);
    }

    public void onFoundMultiple(String[] names, String[] urls) {
        this.foundNames = names;

        Bundle bundle = new Bundle();
        bundle.putStringArray(ResultsFragment.NAMES_KEY, names);
        bundle.putStringArray(ResultsFragment.URLS_KEY, urls);

        vnr = null;

        changeFragment(new ResultsFragment(), bundle, false);
    }

    public void onHTTPFailure(boolean addToBackStack) {
        changeFragment(new FailureFragment(), null, addToBackStack);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
            stopSpeech();
        } else {
            super.onBackPressed();
        }
    }

    private void changeFragment(Fragment newFragment, Bundle arguments, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);

        if (arguments != null)
            newFragment.setArguments(arguments);

        if (addToBackStack)
            transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void updateTTS(Fragment calledFrom, Bundle arguments) {
        stopSpeech();
        enableMenu();

        if (calledFrom instanceof FailureFragment) {
            List<String> speechText = new ArrayList<>();
            speechText.add(getString(R.string.product_not_found));
            setSpeechText(speechText);
        } else if (calledFrom instanceof ProductInformationFragment) {
            List<String> speechText = new ArrayList<>();
            speechText.add(titleText);

            Log.d("WAT", "Parsed null: " + (parsedContentText == null));
            if (parsedContentText != null)
                Log.d("WAT", "Parsed len: " + parsedContentText.length());
            Log.d("WAT", "Full null: " + (fullContentText == null));
            if (fullContentText != null)
                Log.d("WAT", "Full len: " + fullContentText.length());
            Log.d("WAT", "Bundle null: " + (arguments == null));
            if (arguments != null)
                Log.d("WAT", "SHOW_FULL: " + (arguments.getBoolean(SHOW_FULL)));

            if (arguments == null || arguments.getBoolean(SHOW_FULL))
                speechText.addAll(getTextSplitDotsAndNewlines(Html.fromHtml(fullContentText).toString()));
            else
                speechText.addAll(getTextSplitDotsAndNewlines(Html.fromHtml(parsedContentText).toString()));

            setSpeechText(speechText);
        } else if (calledFrom instanceof ResultsFragment) {
            List<String> speechText = new ArrayList<>();
            speechText.add(getString(R.string.search_results));

            if (foundNames != null)
                speechText.addAll(Arrays.asList(foundNames));

            setSpeechText(speechText);
        } else {
            disableMenu();
            setSpeechText(null);
        }
    }

    private String parseContentText(String contentText) {
        contentText = contentText.replaceFirst("<h2></h2>", "");

        Document document = Jsoup.parse(contentText);
        Elements elements = document.getElementsByTag("div").get(0).children();
        Element child;
        String title;
        int start = -1;
        int end = -1;

        for (int i = 0; i < elements.size(); i++) {
            for (int j = 0; j < elements.get(i).children().size(); j++) {
                child = elements.get(i).child(j);

                if (child.tagName().equals("strong") || child.tagName().equals("b")) {
                    title = child.ownText().toLowerCase();

                    if (start == -1) {
                        if (Locale.getDefault().getCountry().equals("SE")) {
                            if (title.contains("graviditet") || title.contains("amning")) {
                                start = i;
                            }
                        } else {
                            if (title.contains("raskaus") || title.contains("raskauden") ||
                                    title.contains("imetys") || title.contains("imetyksen")) {
                                start = i;
                            }
                        }
                    } else {
                        end = i;
                        break;
                    }
                }
            }

            if (end != -1)
                break;
        }

        if (start > -1 && end > -1) {
            String parsed = "";

            List<Element> elementList = elements.subList(start, end);
            for (Element el : elementList)
                parsed = parsed.concat(el.toString());

            return parsed;
        } else { //Did not find the correct section
            return null;
        }
    }

    private String fixHTMLLists(String text) {
        if (text == null)
            return null;

        text = text.replaceAll("<ul>", "");
        text = text.replaceAll("</ul>", "");
        text = text.replaceAll("<ol>", "");
        text = text.replaceAll("</ol>", "");
        text = text.replaceAll("<li>", "-&emsp;");
        text = text.replaceAll("</li>", "<br />");
        return text;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("titleText", titleText);
        outState.putString("fullContentText", fullContentText);
        outState.putString("parsedContentText", parsedContentText);
        outState.putStringArray("foundNames", foundNames);
        outState.putString("vnr", vnr);
        super.onSaveInstanceState(outState);
    }


    public static class LoadingScreenFragment extends Fragment {

        UpdatableTTS callback;

        @Override
        public void onAttach(Context context) {
            callback = (UpdatableTTS) context;
            super.onAttach(context);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            setHasOptionsMenu(false);
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onResume() {
            if (callback != null)
                callback.updateTTS(this, null);

            super.onResume();
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_loading_screen, container, false);
        }
    }


    public static class FailureFragment extends Fragment {

        UpdatableTTS callback;

        @Override
        public void onAttach(Context context) {
            callback = (UpdatableTTS) context;
            super.onAttach(context);
        }

        @Override
        public void onResume() {
            if (callback != null)
                callback.updateTTS(this, null);

            super.onResume();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_product_failure, container, false);
        }
    }


    public static class ProductInformationFragment extends Fragment {

        public static final String TITLE_KEY = "title";
        public static final String VNR_KEY = "vnr";
        public static final String FULL_CONTENT_TEXT_KEY = "fullContentText";
        public static final String PARSED_CONTENT_TEXT_KEY = "parsedContentText";

        UpdatableTTS callback;

        private String titleText;
        private String fullContentText;
        private String parsedContentText;

        private TextView contentText;
        private AppCompatCheckBox checkBox;

        public ProductInformationFragment() { }

        @Override
        public void onAttach(Context context) {
            callback = (UpdatableTTS) context;
            super.onAttach(context);
        }

        @Override
        public void onResume() {
            if (callback != null)
                updateTTS();

            super.onResume();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_product_information, container, false);

            titleText = getArguments().getString(TITLE_KEY);
            ((TextView) view.findViewById(R.id.productTitle)).setText(titleText);

            TextView subTextView = (TextView) view.findViewById(R.id.productSubTitle);
            String subText = getArguments().getString(VNR_KEY);
            if (subText == null)
                subTextView.setVisibility(View.GONE);
            else {
                subText = "Vnr: ".concat(subText);
                subTextView.setText(subText);
            }

            fullContentText = getArguments().getString(FULL_CONTENT_TEXT_KEY);
            parsedContentText = getArguments().getString(PARSED_CONTENT_TEXT_KEY);

            contentText = (TextView) view.findViewById(R.id.productEffects);
            checkBox = (AppCompatCheckBox) view.findViewById(R.id.checkbox);

            if (parsedContentText == null) {
                checkBox.setEnabled(false);
                checkBox.setChecked(true);
            }

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateText();
                }
            });

            updateText();

            return view;
        }

        private void updateText() {
            if (checkBox.isChecked())
                contentText.setText(Html.fromHtml(fullContentText));
            else
                contentText.setText(Html.fromHtml(parsedContentText));

            updateTTS();
        }

        private void updateTTS() {
            Bundle arguments = new Bundle();
            arguments.putBoolean(ProductActivity.SHOW_FULL, checkBox.isChecked());
            callback.updateTTS(this, arguments);
        }
    }


    public static class ResultsFragment extends Fragment {

        public static final String NAMES_KEY = "names";
        public static final String URLS_KEY = "urls";

        UpdatableTTS callback;

        @Override
        public void onAttach(Context context) {
            callback = (UpdatableTTS) context;
            super.onAttach(context);
        }

        @Override
        public void onResume() {
            if (callback != null)
                callback.updateTTS(this, null);

            super.onResume();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_result_list, container, false);

            String[] names = getArguments().getStringArray(NAMES_KEY);
            final String[] urls = getArguments().getStringArray(URLS_KEY);

            final Context context = getContext();

            ListView listView = (ListView) view.findViewById(R.id.result_list);
            listView.setAdapter(new ResultListAdapter(context, names));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HTTPRequests.requestWithUrl((HTTPRequests.HTTPRequestListener)context, urls[position]);
                }
            });

            return view;
        }


        public class ResultListAdapter extends ArrayAdapter<String> {

            private Context context;
            private String[] values;

            public ResultListAdapter(Context context, String[] values) {
                super(context, -1, values);
                this.context = context;
                this.values = values;
            }

            @Override
            public int getCount() {
                return values.length;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolderItem viewHolder;

                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.layout_result_title, parent, false);

                    viewHolder = new ViewHolderItem();
                    viewHolder.textView = (TextView) convertView.findViewById(R.id.name);

                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolderItem) convertView.getTag();
                }

                viewHolder.textView.setText(values[position]);

                return convertView;
            }


        }

        static class ViewHolderItem {
            TextView textView;
        }
    }
}
