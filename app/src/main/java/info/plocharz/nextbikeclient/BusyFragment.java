package info.plocharz.nextbikeclient;

import android.support.v4.app.Fragment;

/**
 * Created by Krzysztof on 2016-08-28.
 */
public class BusyFragment extends Fragment {
    private boolean busy = false;

    boolean isBusy(){
        return this.busy;
    }

    void setBusy(boolean busy){
        this.busy = busy;
    }

}
