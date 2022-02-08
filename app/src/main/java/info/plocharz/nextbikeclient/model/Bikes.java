package info.plocharz.nextbikeclient.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Collection;

@Root(name="nextbike", strict=false)
public class Bikes {

    @ElementList(entry="bike", inline = true, required = false)
    private Collection< Bike > listBike = new ArrayList< Bike >();

    public ArrayList<Bike> getBikes() {
        return new ArrayList<Bike>(listBike);
    }
}
