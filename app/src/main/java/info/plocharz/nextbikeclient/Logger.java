package info.plocharz.nextbikeclient;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

/**
 * Created by Krzysztof on 2016-07-24.
 */
public class Logger {

    private Logger(){
    }

    public static void create(Context context){

    }

    public static int d(String msg) {
        return Log.d(Application.TAG, msg);
    }

    public static int i(String msg) {
        FirebaseCrash.log(msg);
        return Log.i(Application.TAG, msg);
    }

    public static int w(String msg) {
        FirebaseCrash.log(msg);
        return Log.w(Application.TAG, msg);
    }

    public static int e(String msg) {
        FirebaseCrash.log(msg);
        return Log.e(Application.TAG, msg);
    }

    public static int e(String msg, Exception e) {
        FirebaseCrash.log(msg);
        FirebaseCrash.report(e);
        return Log.e(Application.TAG, msg, e);
    }
}
