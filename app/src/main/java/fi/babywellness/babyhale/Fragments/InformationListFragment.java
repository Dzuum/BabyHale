package fi.babywellness.babyhale.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import fi.babywellness.babyhale.InformationActivity;
import fi.babywellness.babyhale.InformationListAdapter;
import fi.babywellness.babyhale.R;

public class InformationListFragment extends Fragment {

    public static InformationListFragment newInstance() {
        return new InformationListFragment();
    }

    public InformationListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_information, container, false);

        InformationListAdapter listAdapter = new InformationListAdapter(
                    getActivity(),
                    getResources().getStringArray(R.array.infoTitlesShort));

        ListView listView = (ListView) layoutView.findViewById(R.id.infoTitleList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), InformationActivity.class);

                if (position == 0)
                    intent.putExtra(InformationActivity.TYPE_KEY, InformationActivity.SHOW_ALCOHOL);
                else if (position == 1)
                    intent.putExtra(InformationActivity.TYPE_KEY, InformationActivity.SHOW_TOBACCO);
                else if (position == 2)
                    intent.putExtra(InformationActivity.TYPE_KEY, InformationActivity.SHOW_DRUGS);

                startActivity(intent);
            }
        });

        return layoutView;
    }
}
