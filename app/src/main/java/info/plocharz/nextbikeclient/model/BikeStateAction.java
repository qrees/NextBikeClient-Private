package info.plocharz.nextbikeclient.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="nextbike", strict=false)
public class BikeStateAction {
    
    @Element(name="bike")
    private Bike bike;
    
    public Bike getBike(){
        return bike;
    }
}
