package info.plocharz.nextbikeclient.model;

import android.content.Context;

public abstract class Operation {
    
    abstract public Long getStartTime();

    public CharSequence toLocalString(Context context) {
        return null;
    }
    

    abstract public Double getCreditAmount();
    
    abstract public boolean isValid();

    abstract public Long getTimestamp();
}
