package info.plocharz.nextbikeclient.requests;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.model.RentalAction;
import info.plocharz.nextbikeclient.model.ReturnAction;

public class ReturnRequest extends NextbikeRequest<ReturnAction> {

    private String rentId;
    private String bikeId;
    private String loginKey;
    private String placeId;

    public ReturnRequest() {
        super(ReturnAction.class);
    }

    public ReturnRequest(String loginKey, String rentId, String bikeId, String placeId) {
        super(ReturnAction.class);
        assert rentId != null;
        assert loginKey != null;
        assert bikeId != null;
        assert placeId != null;
        this.rentId = rentId;
        this.bikeId = bikeId;
        this.loginKey = loginKey;
        this.placeId = placeId;
    }

    @Override
    public String getUrl() {
        return Application.apiBase() + "/api/return.xml";
    }

    @Override
    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data) {
        data.add("loginkey", this.loginKey);
        data.add("rental", this.rentId);
        data.add("bike", this.bikeId);
        data.add("place", this.placeId);
        return data;
    }

}
