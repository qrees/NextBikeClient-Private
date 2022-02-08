package info.plocharz.nextbikeclient.requests;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.model.Nextbike;

public class AccountRequest extends NextbikeRequest<Nextbike> {

    private String loginkey;

    public AccountRequest(String loginkey) {
        super(Nextbike.class);
        this.loginkey = loginkey;
    }

    @Override
    public String getUrl() {
        return Application.apiBase() + "/api/list.xml";
    }

    @Override
    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data) {
        data.add("loginkey", this.loginkey);
        return data;
    }

    public String createCacheKey() {
        return "account";
    }

}
