package info.plocharz.nextbikeclient.requests;

import org.springframework.util.MultiValueMap;

import info.plocharz.nextbikeclient.Application;
import info.plocharz.nextbikeclient.model.Bikes;

/**
 * Created by Krzysztof on 2016-08-07.
 */
public class BikeListRequest extends NextbikeRequest<Bikes> {

    private final String place;

    private BikeListRequest(String uid) {
        super(Bikes.class);
        this.place = uid;
    }

    @Override
    protected String getUrl() {
        return Application.apiBase() + "/api/getBikeList.xml";
    }

    @Override
    protected MultiValueMap<String, String> getData(MultiValueMap<String, String> data) {
        data.add("place", this.place);
        return data;
    }
}
