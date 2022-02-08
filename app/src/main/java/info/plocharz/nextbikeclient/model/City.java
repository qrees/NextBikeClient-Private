package info.plocharz.nextbikeclient.model;

import java.util.ArrayList;
import java.util.Collection;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.google.android.gms.maps.model.LatLng;

@Root(name="city", strict=false)
public class City {

    @ElementList(entry="place", inline=true, required=false)
    private Collection< Place > listPlace = new ArrayList< Place >();
    
    @Attribute
    private String name;

    @Attribute
    private String uid;

    @Attribute
    private String lat;

    @Attribute
    private String lng;
    
    public Collection< Place > getListPlace() {
        for (Place place: this.listPlace){
            place.setCity(this);
        }
        return listPlace;
    }
    

    public boolean hasLocation(){
        return (this.lat != null && this.lng != null);
    }
    
    public double getLat() {
        if(this.lat == null)
            return 0;
        else
            return Float.parseFloat(this.lat);
    }
    
    public double getLng() {
        if(this.lng == null)
            return 0;
        else
            return Float.parseFloat(this.lng);
    }

    public String getName() {
        return this.name;
    }
}
