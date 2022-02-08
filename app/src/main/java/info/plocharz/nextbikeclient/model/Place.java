package info.plocharz.nextbikeclient.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;


@Root(strict = false)
public class Place {

    private City city;

    void setCity(City city){
        this.city = city;
    }

    public City getCity(){
        return this.city;
    }

    @Attribute(name="bikes")
    private String bikes;
    
    @Attribute(name="uid")
    private String uid;
    
    @Attribute
    private String lat;

    @Attribute
    private String lng;
    
    @Attribute
    private String name;

    @Attribute(required=false)
    private String number;

    private Marker marker = null;
    
    public String getName(){
        return name;
    }
    
    @Override
    public String toString() {
        return this.getName();
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
    
    public LatLng getLatLng(){
        return new LatLng(this.getLat(), this.getLng());
    }

    public double getLng() {
        if(this.lng == null)
            return 0;
        else
            return Float.parseFloat(this.lng);
    }

    public String getUid() {
        return uid;
    }
    
    public String getBikes(){
        return this.bikes;
    }

    public String getNumber(){
        return this.number;
    }
}
