package info.plocharz.nextbikeclient.model;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="user", strict=false)
public class User {

    @Attribute
    private String loginkey;

    @Attribute(required=false)
    private Integer credits;

    @Attribute(required=false)
    private String currency;
    
    @Attribute(required=false)
    private String mobile;
    
    public String getLoginkey(){
        return this.loginkey;
    }
    
    public Integer getCredits(){
        return this.credits;
    }

    public String getMobile(){
        return this.mobile;
    }
    
    public String getCurreny(){
        return this.currency;
    }
}
