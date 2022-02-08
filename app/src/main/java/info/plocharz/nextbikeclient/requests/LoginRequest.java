package info.plocharz.nextbikeclient.requests;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.model.Nextbike;

public class LoginRequest extends NextbikeRequest<Nextbike> {

    public LoginRequest() {
        super(Nextbike.class);
    }

    private String mobile;
    private String pin;
    
    public LoginRequest(String mobile, String pin) {
        super(Nextbike.class);
        this.mobile = mobile;
        this.pin = pin;
    }
    
    @Override
    public String getUrl() {
        return Application.apiBase() + "/api/login.xml";
    }

    @Override
    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data) {
        data.add("mobile", this.mobile);
        data.add("pin", this.pin);
        return data;
    }
}
