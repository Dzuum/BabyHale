package fi.babywellness.babyhale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InformationListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public InformationListAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.layout_information_title, parent, false);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.infoIcon);
        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.ic_alcohol);
                break;
            case 1:
                imageView.setImageResource(R.drawable.ic_tobacco);
                break;
            case 2:
                imageView.setImageResource(R.drawable.ic_drugs);
                break;
        }

        TextView textView = (TextView) rowView.findViewById(R.id.infoTitle);
        textView.setText(values[position]);

        return rowView;
    }
}
