package fi.babywellness.babyhale.Fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import fi.babywellness.babyhale.MainActivity;
import fi.babywellness.babyhale.R;

public class SearchFragment extends Fragment {

    TabLayout tabs;
    Fragment scanFragment;
    Fragment textFragment;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    public SearchFragment() {
        scanFragment = new ScanFragment();
        textFragment = new SearchTextFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        tabs = (TabLayout) view.findViewById(R.id.tabs_bottom);
        tabs.addTab(tabs.newTab().setText(R.string.menu_scan));
        tabs.addTab(tabs.newTab().setText(R.string.menu_text));

        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                replaceFragmentWithCurrent();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        replaceFragmentWithCurrent();

        return view;
    }

    private void replaceFragmentWithCurrent() {
        int position = tabs.getSelectedTabPosition();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ((MainActivity)getActivity()).stopSpeech();

        List<String> textList = new ArrayList<>();
        if (position == 0) {
            fragmentTransaction.replace(R.id.fragment_container, scanFragment);
            textList.add(getString(R.string.search_scan_help));
        } else if (position == 1) {
            fragmentTransaction.replace(R.id.fragment_container, textFragment);
            textList.add(getString(R.string.search_text_help));
        }
        ((MainActivity)getActivity()).setSearchSpeechText(textList);
        ((MainActivity)getActivity()).setSpeechText(textList);

        fragmentTransaction.commit();
    }
}
