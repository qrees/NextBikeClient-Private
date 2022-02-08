package info.plocharz.nextbikeclient.model;

import info.plocharz.nextbikeclient.R;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import android.content.Context;

@Root(name="rental", strict=false)
public class Rental extends Operation {
    
    @Attribute
    private String id;
    
    @Attribute
    private Long start_time;

    @Attribute(required=false)
    private Long end_time;
    
    @Attribute(required=false)
    private String bike;
    
    @Attribute(required=false)
    private Integer price;
    
    @Attribute(required=false)
    private String code;

    @Attribute(required=false)
    private String start_place;
    
    @Attribute(required=false)
    private String end_place;
    
    @Attribute(required=false)
    private String customer_rfids;
    
    public String getId(){
        return this.id;
    }
    
    @Override
    public Long getStartTime() {
        return this.start_time;
    }

    public Long getEndTime() {
        return this.end_time;
    }
    
    public String getUnlockCode(){
        return this.code;
    }
    
    public String getBikeId(){
        return this.bike;
    }
    
    public String getStartPlace(){
        return this.start_place;
    }

    public String getEndPlace(){
        return this.end_place;
    }
    
    public CharSequence toLocalString(Context context) {
        return this.bike;
    }
    
    public Double getCreditAmount(){
        if (this.price != null)
            return - (double) (this.price)/100;
        else
            return null;
    }
    
    @Override
    public boolean isValid() {
        return !this.id.equals("0");
    }

    public boolean isReturned() {
        return this.end_place != null && (!this.end_place.equals(""));
    }
    
    @Override
    public Long getTimestamp() {
        return start_time * 1000;
    }
}
