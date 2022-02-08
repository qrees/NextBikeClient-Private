package info.plocharz.nextbikeclient;

import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import info.plocharz.nextbikeclient.model.Bike;
import info.plocharz.nextbikeclient.model.Bikes;
import info.plocharz.nextbikeclient.model.Place;
import info.plocharz.nextbikeclient.orm.Station;

/**
 * Created by Krzysztof on 2016-07-24.

 */
public class StationInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;
    private final MapFragment.MyClusterRenderer mRenderer;
    private final MapFragment fragment;

    StationInfoAdapter(MapFragment mapFragment, MapFragment.MyClusterRenderer renderer) {
        this.fragment = mapFragment;
        this.myContentsView = mapFragment.getActivity().getLayoutInflater().inflate(
                R.layout.info_window, null);
        this.mRenderer = renderer;
    }

    @Override
    public View getInfoContents(Marker marker) {
        MapFragment.StationClusterItem item = this.mRenderer.getClusterItem(marker);
        Station station = item.getStation();
        Logger.d("Show Info window for " + station);
        TextView station_name_view = (TextView)myContentsView.findViewById(R.id.station_name);
        station_name_view.setText(station.getName());

        Bikes bikes = this.fragment.markerToBikes.get(marker);
        GridView bikeListView = (GridView) myContentsView.findViewById(R.id.bike_list);
        if(bikes != null) {
            ArrayList<Bike> bike_list = bikes.getBikes();
            Logger.d(String.format("Got %s bikes", bike_list.size()));
            bikeListView.setAdapter(new BikeAdapter(this.fragment.getActivity(), bike_list));


            bikeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Toast.makeText(fragment.getContext(), "item: " + position, Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            bikeListView.setAdapter(null);
        }
        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

}
