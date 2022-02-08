package info.plocharz.nextbikeclient.requests;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.model.Nextbike;
import info.plocharz.nextbikeclient.model.RentalAction;

public class RentRequest extends NextbikeRequest<RentalAction> {

    private String bikeId;
    private String loginKey;
    
    public RentRequest() {
        super(RentalAction.class);
    }

    public RentRequest(String loginKey, String bikeId) {
        super(RentalAction.class);
        assert bikeId != null;
        assert loginKey != null;
        this.bikeId = bikeId;
        this.loginKey = loginKey;
    }

    @Override
    public String getUrl() {
        return Application.apiBase() + "/api/rent.xml";
    }

    @Override
    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data) {
        data.add("loginkey", this.loginKey);
        data.add("bike", this.bikeId);
        return data;
    }
}
