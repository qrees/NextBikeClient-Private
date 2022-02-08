package info.plocharz.nextbikeclient.model;

import info.plocharz.nextbikeclient.Application;

import java.util.ArrayList;
import java.util.Collection;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;


@Root(name="markers", strict=false)
public class Markers {

    @ElementList(entry="country", inline = true, required = false)
    private Collection< Country > listCountry = new ArrayList< Country >();

    public Collection< Country > getListCountry() {
        return listCountry;
    }
    
    public ArrayList<Place> getPlaces(){
        ArrayList<Place> places = new ArrayList<Place>();
        for(Country country: listCountry){
            for(City city: country.getListCity()){
                places.addAll(city.getListPlace());
            }
        }
        return places;
    }

}
