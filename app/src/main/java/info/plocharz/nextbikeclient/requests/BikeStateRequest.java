package info.plocharz.nextbikeclient.requests;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.model.BikeStateAction;

public class BikeStateRequest extends NextbikeRequest<BikeStateAction> {

    private String bikeId;
    
    public BikeStateRequest(String bikeId) {
        super(BikeStateAction.class);
        assert bikeId != null;
        this.bikeId = bikeId;
    }

    @Override
    public String getUrl() {
        return Application.apiBase() + "/api/getBikeState.xml";
    }

    @Override
    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data) {
        data.add("bike", this.bikeId);
        return data;
    }
}
