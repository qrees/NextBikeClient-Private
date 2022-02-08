package info.plocharz.nextbikeclient.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;


@Root(name="nextbike", strict=false)
public class Nextbike {
    
    @Element(name="user", required=false)
    private User user;
    
    @Element(required=false)
    private Account account;
    
    public User getUser(){
        return this.user;
    }
    
    public Account getAccount(){
        return this.account;
    }
}
