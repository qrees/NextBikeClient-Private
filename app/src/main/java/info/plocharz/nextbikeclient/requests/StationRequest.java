package info.plocharz.nextbikeclient.requests;

import java.util.ArrayList;

import android.text.TextUtils;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.Logger;
import info.plocharz.nextbikeclient.model.Markers;


public class StationRequest extends NextbikeRequest<Markers> {

    private Double distance;
    private Boolean list_cities = false;
    private Double lat;
    private Double lng;
    private Integer limit;

    public StationRequest() {
        super(Markers.class);
    }

    public StationRequest(Double lat, Double lng, Integer limit, Boolean list_cities) {
        super(Markers.class);
        this.lat = lat;
        this.lng = lng;
        this.limit = limit;
        this.list_cities = list_cities;
    }

    public StationRequest(Double lat, Double lng, Double distance) {
        super(Markers.class);
        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }

    public static StationRequest StationRequestCities() {
        return new StationRequest(
                null, null, null, false
        );
    }
    
    public String createCacheKey() {
        return "stations";
    }

    @Override
    public String getUrl() {
        String url = String.format(Application.apiBase() + "/maps/nextbike-official.xml?");
        ArrayList<String> tokens = new ArrayList<String>();
        if(this.lat != null){
            tokens.add("lat="+this.lat.toString());
        }
        if(this.lng != null){
            tokens.add("lng="+this.lng.toString());
        }
        if(this.limit != null){
            tokens.add("lat="+this.limit.toString());
        }
        if(this.list_cities){
            tokens.add("list_cities=1");
        }
        if(this.distance != null){
            tokens.add("distance=" + this.distance.toString());
        }
        String params = TextUtils.join("&", tokens);
        return url+params;
    }

    protected boolean usePost(){
        return false;
    }
}
