package info.plocharz.nextbikeclient.orm;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.maps.model.LatLng;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = AppDatabase.class)
public class Station extends BaseModel {

    @PrimaryKey()
    public String uid;

    @Column
    public double lat;

    @Column
    public double lng;

    @Column
    public String name;

    @Column
    public String city;

    @Column
    public String number;

    public double getLat(){
        return this.lat;
    }

    public double getLng(){
        return this.lng;
    }

    public String getUid(){
        return this.uid;
    }

    public LatLng getLatLng(){
        return new LatLng(this.getLat(), this.getLng());
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
