package info.plocharz.nextbikeclient;

import info.plocharz.nextbikeclient.orm.Station;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.octo.android.robospice.SpiceManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

public class MarkersStorage {

    private SpiceManager spiceManager;

    public MarkersStorage(SpiceManager spiceManager) {
        this.spiceManager = spiceManager;
    }

    public List<Station> getMarkers(){
        List<Station> stations = SQLite.select()
                .from(Station.class)
                .queryList();
        return stations;
    }

    public ArrayList<Station> getPlacesNear(LatLng latLng, Double distance){
        ArrayList<Station> places = new ArrayList<Station>();
        for(Station station: this.getMarkers()){
            if(Application.distance(station.getLatLng(), latLng) < distance)
                places.add(station);
        }

        return places;
    }


}
