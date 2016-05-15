package fi.babywellness.babyhale.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import fi.babywellness.babyhale.ProductActivity;
import fi.babywellness.babyhale.R;

public class SearchTextFragment extends Fragment {

    Button searchButton;
    EditText searchText;

    public static SearchTextFragment newInstance() {
        return new SearchTextFragment();
    }

    public SearchTextFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_search_text, container, false);

        searchButton = (Button) layout.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearch();
            }
        });

        searchText = (EditText) layout.findViewById(R.id.searchText);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearch();
                    handled = true;
                }
                return handled;
            }
        });

        return layout;
    }

    private void onSearch() {
        Intent intent = new Intent(getContext(), ProductActivity.class);

        String text = searchText.getText().toString();
        if (text.trim().isEmpty()) {
            Toast.makeText(getContext(), R.string.empty_search, Toast.LENGTH_SHORT).show();
        } else {
            if (text.replaceAll(" ", "").matches("[0-9]+"))
                intent.putExtra(ProductActivity.VNR_SEARCH, text.replaceAll(" ", ""));
            else
                intent.putExtra(ProductActivity.NAME_SEARCH, text);

            startActivity(intent);
        }
    }
}
