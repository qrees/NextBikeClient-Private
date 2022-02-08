package info.plocharz.nextbikeclient.model;

import info.plocharz.nextbikeclient.R;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import android.content.Context;

@Root(name="transaction", strict=false)
public class Transaction extends Operation {

    @Attribute
    private Long date;

    @Attribute
    private Integer amount;

    @Override
    public Long getStartTime() {
        return this.date;
    }
    
    public CharSequence toLocalString(Context context) {
        return context.getString(R.string.credits_added);
    }
    
    @Override
    public boolean isValid() {
        return this.date != 0;
    }

    public Double getCreditAmount(){
        return (double) (this.amount)/100;
    }

    @Override
    public Long getTimestamp() {
        return date * 1000;
    }
}
