package fi.babywellness.babyhale;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fi.babywellness.babyhale.Fragments.InformationListFragment;
import fi.babywellness.babyhale.Fragments.SearchFragment;

public class MainActivity extends BaseActivity {

    private List<String> searchSpeechText = new ArrayList<>();

    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar(false);
        setToolbarTitle(R.string.title_activity_main);
        pager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter adapter = new TabsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                stopSpeech();
                if (position == 0)
                    setSpeechText(searchSpeechText);
                else if (position == 1) {
                    setSpeechText(Arrays.asList(getResources().getStringArray(R.array.infoTitlesShort)));
                }
            }
        });

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(pager);

    }

    public void setSearchSpeechText(List<String> textList) {
        searchSpeechText = textList;
    }

    public void setSpeechText(List<String> textList) {
        super.setSpeechText(textList);
    }

    public void stopSpeech() {
        super.stopSpeech();
    }

    @Override
    protected void onDestroy() {
        shutdownTTS();
        super.onDestroy();
    }


    public class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return SearchFragment.newInstance();
            } else if (position == 1) {
                return InformationListFragment.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.scanTabTitle);
            } else if (position == 1) {
                return getString(R.string.infoTabTitle);
            }

            return "";
        }
    }
}
