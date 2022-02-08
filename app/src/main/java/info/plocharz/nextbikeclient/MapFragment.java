package info.plocharz.nextbikeclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import info.plocharz.nextbikeclient.model.Bikes;
import info.plocharz.nextbikeclient.model.Markers;
import info.plocharz.nextbikeclient.model.Place;
import info.plocharz.nextbikeclient.orm.Station;
import info.plocharz.nextbikeclient.requests.StationRequest;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.ui.IconGenerator;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MapFragment 
    extends SupportMapFragment
    implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<MapFragment.StationClusterItem> {
    private Application app;
    private ClusterManager<StationClusterItem> mClusterManager;
    private CameraPosition savedPosition;
    private MyClusterRenderer renderer;
    public Map<Marker, Bikes> markerToBikes = new HashMap<>();
    private GoogleMap googleMap;
    private HashMap<String, StationClusterItem> station_to_cluster_item = new HashMap<>();
    private IconGenerator icon_generator;
    private HashMap<String, BitmapDescriptor> icon_descriptors = new HashMap<>();
    private HashMap<String, String> uid_to_bikes = new HashMap<>();

    public MapFragment(){
        super();
    }

    void setBusy(boolean busy){

    }

    public static MapFragment createInstance(){
        return new MapFragment();
    }

    private BitmapDescriptor getIcon(String bike_count){
        BitmapDescriptor descriptor = this.icon_descriptors.get(bike_count);
        if(descriptor == null){
            if (bike_count.equals("0")) {
                icon_generator.setStyle(IconGenerator.STYLE_RED);
            } else {
                icon_generator.setStyle(IconGenerator.STYLE_GREEN);
            }
            Bitmap icon = icon_generator.makeIcon(bike_count);
            descriptor = BitmapDescriptorFactory.fromBitmap(icon);
            this.icon_descriptors.put(bike_count, descriptor);
        }
        return this.icon_descriptors.get(bike_count);
    }

    private BitmapDescriptor getDefaultIcon(){
        String bike_count = "?";
        BitmapDescriptor descriptor = this.icon_descriptors.get(bike_count);
        if(descriptor == null){
            icon_generator.setStyle(IconGenerator.STYLE_BLUE);
            Bitmap icon = icon_generator.makeIcon(bike_count);
            descriptor = BitmapDescriptorFactory.fromBitmap(icon);
            this.icon_descriptors.put(bike_count, descriptor);
        }
        return this.icon_descriptors.get(bike_count);
    }

    private BitmapDescriptor getIcon(StationClusterItem cluster_item){
        String bikes = uid_to_bikes.get(cluster_item.getStation().getUid());
        if (bikes == null){
            return MapFragment.this.getDefaultIcon();
        }
        return MapFragment.this.getIcon(bikes);
    }

    private class StationListener implements RequestListener<info.plocharz.nextbikeclient.model.Markers> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {

        }

        @Override
        public void onRequestSuccess(Markers markers) {
            Logger.i("StationListener.onRequestSuccess()");
            ArrayList<Place> places = markers.getPlaces();
            Set<StationClusterItem> expected_items = new HashSet<>(station_to_cluster_item.values());
            for(Place place: places){
                String uid = place.getUid();
                String bikes = place.getBikes();
                uid_to_bikes.put(uid, bikes);
                StationClusterItem cluster_item = station_to_cluster_item.get(uid);
                expected_items.remove(cluster_item);
                if (cluster_item == null){
                    MapFragment.this.addMissingStation(place);
                } else {
                    Marker marker = renderer.getMarker(cluster_item);
                    if (marker != null) {
                        marker.setIcon(MapFragment.this.getIcon(cluster_item));
                    }
                }
            }
            Logger.d("Did not receive data for: " + String.valueOf(expected_items.size()));
            for (StationClusterItem item : expected_items) {
                Logger.d("Did not receive data for: " + item.getStation().getName());
                MapFragment.this.removeStation(item);
            };
            mClusterManager.cluster();
        }
    }

    private class StationUpdateTask extends TimerTask {

        @Override
        public void run() {
            StationRequest station_request = new StationRequest();
            SpiceManager spiceManager = getSpiceManager();
            spiceManager.execute(station_request, new StationListener());
        }
    }

    class StationClusterItem implements ClusterItem {
        private final Station station;

        StationClusterItem(Station place){
            station = place;
        }

        @Override
        public LatLng getPosition() {
            return new LatLng(station.getLat(), station.getLng());
        }

        Station getStation() {
            return station;
        }
    }

    class MyClusterRenderer extends DefaultClusterRenderer<StationClusterItem> implements GoogleMap.OnCameraChangeListener{

        protected boolean shouldRenderAsCluster(Cluster<StationClusterItem> cluster) {
            return cluster.getSize() > 2;
        }

        MyClusterRenderer(Context context, GoogleMap map, ClusterManager<StationClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        protected String getClusterText(int bucket) {
            if(bucket < 11 & bucket > 0)
                return String.valueOf(bucket);
            else
                return String.valueOf(bucket) + "+";
        }

        @SuppressLint("DefaultLocale")
        protected void onBeforeClusterRendered(Cluster<StationClusterItem> cluster, MarkerOptions markerOptions) {
            int bucket = this.getBucket(cluster);
            BitmapDescriptor descriptor = this.mIcons.get(bucket);
            if(descriptor == null) {
                this.mColoredCircleBackground.getPaint().setColor(this.getColor(bucket));
                Bitmap bitmap = this.mIconGenerator.makeIcon(this.getClusterText(bucket));
                descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                this.mIcons.put(bucket, descriptor);
            }

            markerOptions.icon(descriptor);
        }

        @Override
        protected void onBeforeClusterItemRendered(StationClusterItem item, MarkerOptions markerOptions) {
            BitmapDescriptor icon = MapFragment.this.getIcon(item);
            if (icon == null){
                return;
            }
            markerOptions.position(item.getPosition());
            markerOptions.icon(icon);
            markerOptions.anchor(0.5f, 1f);
        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

        }
    }

    private class BikeMarkerManager extends MarkerManager {

        private final MapFragment fragment;

        BikeMarkerManager(GoogleMap map, MapFragment fragment) {
            super(map);
            this.fragment = fragment;
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            MapFragment.StationClusterItem item = this.fragment.getClusterItem(marker);
            if (item != null) {
                Station station = item.getStation();
                return false;
            }
            return super.onMarkerClick(marker);
        }
    }

    private StationClusterItem getClusterItem(Marker marker) {
        return renderer.getClusterItem(marker);
    }

    private void setupMap() {
        final Activity activity = getActivity();
        BikeMarkerManager markerManager = new BikeMarkerManager(googleMap, MapFragment.this);
        mClusterManager = new ClusterManager<>(activity, googleMap, markerManager);

        Algorithm mAlgorithm = new PreCachingAlgorithmDecorator(new NonHierarchicalDistanceBasedAlgorithm());
        mClusterManager.setAlgorithm(mAlgorithm);
        MapFragment.this.renderer = new MyClusterRenderer(activity, googleMap, mClusterManager);
        mClusterManager.setRenderer(renderer);

        googleMap.setOnCameraChangeListener(mClusterManager);

        MarkersStorage markersStorage = MapFragment.this.app.getMarkers();
        this.processMarkers(markersStorage.getMarkers());

        Runnable task = new StationUpdateTask();
        task.run();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        Logger.i("MapFragment onAttach");
        icon_generator = new IconGenerator(MapFragment.this.getContext());
        super.onAttach(activity);
        this.app = (Application) activity.getApplication();
    }

    public SpiceManager getSpiceManager(){
        return this.app.getSpiceManager();
    }

    private void trySetMyLocationEnabled(GoogleMap map, boolean value){
        try {
            map.setMyLocationEnabled(value);
            Logger.i(String.format("setMyLocationEnabled = %b", value));
        } catch (SecurityException ignored) {
            Logger.i("Failed to setMyLocationEnabled");
        }
    }

    @Override
    public void onStop() {
        if (this.googleMap != null){
            this.trySetMyLocationEnabled(googleMap, false);
            this.savedPosition = googleMap.getCameraPosition();
        };
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.googleMap != null){
            restoreMap();
        };
    }

    private void restoreMap(){
        Location location;
        Activity activity = getActivity();
        this.trySetMyLocationEnabled(googleMap, true);
        location = ((Application) activity.getApplication()).getBestLocation();

        if (this.savedPosition != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(this.savedPosition);
            googleMap.moveCamera(update);
        } else if(location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            CameraUpdate center = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13);
            Logger.d("Zooming to: " + ((Double)latitude).toString() + " " + ((Double)longitude).toString());
            googleMap.animateCamera(center);
        } else {
            Logger.w("Location is not available");
        }
    }

    private void removeStation(StationClusterItem item) {
        Logger.i("removeStation: " + item.getStation().toString());
        mClusterManager.removeItem(item);
        Station station = item.getStation();
        station_to_cluster_item.remove(station.getUid());
        station.delete();
    }

    private void addMissingStation(Place place){
        Logger.i("addMissingStation: " + place.toString());
        Station station = new Station();
        station.number = place.getNumber();
        station.lng = place.getLng();
        station.lat = place.getLat();
        station.name = place.getName();
        station.city = place.getCity().getName();
        station.uid = place.getUid();
        station.save();
        StationClusterItem item = new StationClusterItem(station);
        station_to_cluster_item.put(station.getUid(), item);
        mClusterManager.addItem(item);
    }

    public void processMarkers(final List<Station> stations) {
        Logger.d("Processing places " + stations.size());
        mClusterManager.clearItems();
        station_to_cluster_item = new HashMap<>();
        for(Station station: stations){
            StationClusterItem item = new StationClusterItem(station);
            station_to_cluster_item.put(station.getUid(), item);
            mClusterManager.addItem(item);
        }
        mClusterManager.cluster();
        Logger.d("Update map after processing markers");
        setBusy(false);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        restoreMap();
        setBusy(true);
        setupMap();
    }

    @Override
    public boolean onClusterItemClick(StationClusterItem stationClusterItem) {
        Logger.d("Cluster item clicked: " + stationClusterItem.getStation());
        return true;
    }
}
