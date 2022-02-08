package info.plocharz.nextbikeclient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.android.gms.maps.model.LatLng;
import com.octo.android.robospice.XmlSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;

import info.plocharz.nextbikeclient.orm.DataBaseHelper;

public class Application extends MultiDexApplication {

    public static final String DATA_VERSION = "DATA_VERSION";
    public static final String API_KEY = "";
    public static final String TAG = "NextBikeClient";

    private Auth auth;
    protected SpiceManager spiceManager = new SpiceManager(
            XmlSpringAndroidSpiceService.class);
    private MarkersStorage markersStorage;

    public static String apiBase() {
        return "https://nextbike.net";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.create(getApplicationContext());
        Logger.i("Started application");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);

        try {
            this.checkVersion();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not get current app version", e);
        }

        this.auth = new Auth(pref, spiceManager);
        this.verifyAuth();

        this.markersStorage = new MarkersStorage(this.spiceManager);
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
        FlowConfig.Builder flow_config = new FlowConfig.Builder(this).openDatabasesOnInit(true);
        FlowManager.init(flow_config.build());
    }

    @Override
    public void onTerminate() {
        Logger.i("Terminated application");
        super.onTerminate();
    }

    private void checkVersion() throws NameNotFoundException, InterruptedException, ExecutionException {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        int version = pInfo.versionCode;

        int previousVersion = pref.getInt(Application.DATA_VERSION, 0);
        if (version != previousVersion) {
            this.onUpdate(version, previousVersion);
        }

        Editor editor = pref.edit();
        editor.putInt(Application.DATA_VERSION, version);
        editor.apply();
    }

    private void onUpdate(int version, int previousVersion) throws InterruptedException, ExecutionException {
        Logger.i(String.format("Updating from %s to %s", previousVersion, version));
        Future<?> future = this.spiceManager.removeAllDataFromCache();
        if (future != null) {
            future.get();
        }
        DataBaseHelper helper = new DataBaseHelper(this);
        try {
            helper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void verifyAuth() {
        this.auth.verify();
    }

    public SpiceManager getSpiceManager() {
        return this.spiceManager;
    }

    public Auth getAuth() {
        return this.auth;
    }

    public MarkersStorage getMarkers() {
        return this.markersStorage;
    }

    public Location getBestLocation() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = locationManager.getProviders(true);
        Location location;
        for (int i = providers.size() - 1; i >= 0; i--) {
            try{
                location = locationManager.getLastKnownLocation(providers.get(i));
            } catch (SecurityException exc) {
                Logger.i("Failed to get location from providers");
                location = null;
            }
            if (location != null)
                return location;
        }
        return null;
    }

    public boolean hasLocationPermission(){
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    public static double distance (LatLng latLngA, LatLng latLngB) {
        double lat_a = latLngA.latitude;
        double lng_a = latLngA.longitude;
        double lat_b = latLngB.latitude;
        double lng_b = latLngB.longitude;
        
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
        Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }
    
}
