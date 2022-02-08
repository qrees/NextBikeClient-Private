package info.plocharz.nextbikeclient.model;

import java.util.ArrayList;
import java.util.Collection;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="country", strict=false)
public class Country {
    
    @Attribute
    private String country_name;
    
    @ElementList(entry="city", inline=true, required=false)
    private Collection< City > listCity = new ArrayList< City >();

    public Collection< City > getListCity() {
        return listCity;
    }
    
    public String getName(){
        return this.country_name;
    }
    
}
