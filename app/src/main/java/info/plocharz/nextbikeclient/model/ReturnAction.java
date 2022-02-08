package info.plocharz.nextbikeclient.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="nextbike", strict=false)
public class ReturnAction {
    
    @Element(name="rental", type=Rental.class)
    private Rental rental;
    
    public Rental getRental(){
        return rental;
    }
}
