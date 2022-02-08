package info.plocharz.nextbikeclient.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(strict = false)
public class Bike {

    @Attribute(name="number")
    private String number;

    @Attribute(name="state")
    private String state;

    @Attribute(name="bike_type", required = false)
    private String bike_type;

    public String getNumber() {
        return number;
    }

    public String getState() {
        return state;
    }
}
