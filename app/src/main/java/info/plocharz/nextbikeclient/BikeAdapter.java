package info.plocharz.nextbikeclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import info.plocharz.nextbikeclient.model.Bike;

/**
 * Created by Krzysztof on 2016-07-26.
 */
public class BikeAdapter extends ArrayAdapter<Bike> implements View.OnClickListener {

    public BikeAdapter(Context context, ArrayList<Bike> items) {
        super(context, R.layout.bike_item, items);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.bike_item, null);
        }

        TextView bike_id_text = (TextView)v.findViewById(R.id.bike_id);
        bike_id_text.setText(this.getItem(position).getNumber());
        return v;
    }
}
