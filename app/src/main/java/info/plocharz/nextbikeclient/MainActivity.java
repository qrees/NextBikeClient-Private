package info.plocharz.nextbikeclient;


import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.octo.android.robospice.SpiceManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements Observer {
    static final String INTENT_ACTION = "action";
    static final String ACTION_RENT = "rent";
    private static final String MENU_ACCOUNT = "account";
    private static final String MENU_MAP = "map";
    private static final String MENU_HISTORY = "history";
    private static final String MENU_RENT = "rent";
    private static final String MENU_BARCODE = "barcode";
    public static final String RENT_ID = "rent_id";
    public static final int REQUEST_LOCATION_PERMISSION = 0;
    private static final String TAG = "MainActivity";
    private HashMap<String, Fragment> fragments = new HashMap<>();
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private SpiceManager spiceManager;
    private Toolbar toolbar;

    private ActionBarDrawerToggle mDrawerToggle;
    private String pending_switch_to = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        fragments.put(MENU_MAP, MapFragment.createInstance());
        fragments.put(MENU_ACCOUNT, new AccountFragment());
        fragments.put(MENU_HISTORY, new HistoryFragment());
        fragments.put(MENU_RENT, new RentFragment());
        fragments.put(MENU_BARCODE, new BarcodeScannerFragment());

        setContentView(R.layout.activity_main);
        this.spiceManager = this.getMyApplication().getSpiceManager();
        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);

        this.initDrawer();
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        this.getMyApplication().getAuth().addObserver(this);
        
        if (savedInstanceState == null) {
            if(this.processIntent()){
                
            } else {
                Logger.i("!! Showing map");
                this.showMap();
            }
        }

        requestLocationPermission();
        Logger.i("MainActivity onCreate");
    }

    public void requestLocationPermission(){
        if (getMyApplication().hasLocationPermission()) {
            Logger.i("no location permission, asking user");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            Logger.i("Location permission granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logger.i("Location permission was granted");
                } else {
                    Logger.i("Location permission was denied");
                }
            }
        }
    }

    public boolean processUri(Uri uri){
        return processUri(uri, false);
    }

    public boolean processUri(Uri uri, boolean with_back_button){
        String path = uri.getPath();
        String bikeId = null;
        Pattern p1 = Pattern.compile("/b/(\\d{5})");
        Pattern p2 = Pattern.compile("/(\\d{5})");
        Matcher m1 = p1.matcher(path);
        Matcher m2 = p2.matcher(path);
        if(m1.matches()){
            bikeId = m1.group(1);
        }

        if(m2.matches()){
            bikeId = m2.group(1);
        }

        if(bikeId != null){
            Logger.i("!! Renting: " + bikeId);
            RentFragment fragment = (RentFragment)this.fragments.get(MENU_RENT);
            fragment.setBikeId(bikeId);
            this.loginAndSwitchTo(MENU_RENT, with_back_button);
            return true;
        } else {
            Logger.e("Pattern not recognized: " + path);
        }
        return false;
    }

    private boolean processIntent(){
        Intent intent = getIntent();
        String action = intent.getAction();
        
        
        if(Intent.ACTION_VIEW.equals(action)){
            Uri intentUri = intent.getData();
            return this.processUri(intentUri);
        }
        return false;
    }
    
    private void initDrawer(){

        DrawerMenuItem[] menuItems = new DrawerMenuItem[]{
            new DrawerMenuItem(getResources().getString(R.string.Map), MENU_MAP),
            new DrawerMenuItem(getResources().getString(R.string.Account), MENU_ACCOUNT),
            new DrawerMenuItem(getResources().getString(R.string.History), MENU_HISTORY),
            new DrawerMenuItem(getResources().getString(R.string.Rent), MENU_RENT),
            new DrawerMenuItem(getResources().getString(R.string.Barcode), MENU_BARCODE)
        };
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<DrawerMenuItem>(this, R.layout.drawer_list_item, menuItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mDrawerList.setItemChecked(MainActivity.this.getDefaultDrawerItem(), true);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void login_success() {
        if (this.pending_switch_to != null){
            this.loginAndSwitchTo(this.pending_switch_to);
        }
    }

    private class DrawerMenuItem {
        private String name;
        private String tag;

        DrawerMenuItem(String name, String tag){
            this.name = name;
            this.tag = tag;
        }
        
        @Override
        public String toString(){
            return this.name;
        }
        
        public String getTag(){
            return this.tag;
        }
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DrawerMenuItem item = (DrawerMenuItem)parent.getItemAtPosition(position);
            String tag = item.getTag();
            Logger.i("Switching to: " + tag);
            switch (tag) {
                case MENU_ACCOUNT:
                case MENU_HISTORY:
                case MENU_RENT:
                    MainActivity.this.loginAndSwitchTo(tag);
                break;
                case MENU_BARCODE:
                    MainActivity.this.switchTo(tag);
                break;

            case MENU_MAP:
                MainActivity.this.showMap();
                break;
            default:
                break;
            }

            MainActivity.this.mDrawerLayout.closeDrawer(MainActivity.this.mDrawerList);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void switchTo(String fragmentTag){
        switchTo(fragmentTag, false);
    }

    public void switchTo(String fragmentTag, boolean with_back_button){
        Logger.i("Switching to " + fragmentTag);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = this.fragments.get(fragmentTag);
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
    
    public void loginAndSwitchTo(final String fragmentTag){
        loginAndSwitchTo(fragmentTag, false);
    }

    public void loginAndSwitchTo(final String fragmentTag, final boolean with_back_button){
        this.pending_switch_to = null;
        this.setSupportProgressBarIndeterminateVisibility(true);
        final Auth auth = this.getMyApplication().getAuth();
        Logger.d("loginAndSwitchTo");
        auth.verify(new Auth.AuthListener() {
            public void onDone() {
                Logger.i("Login success");
                setSupportProgressBarIndeterminateVisibility(false);
                MainActivity.this.switchTo(fragmentTag, with_back_button);
            }

            public void onLogout() { }

            public void onFailure() {
                if (auth.isUnableToVerify()) {
                    Logger.i("Login fail, and unable to verify");
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, R.string.no_network, duration);
                    toast.show();
                } else {
                    Logger.i("Login fail, show dialog");
                    MainActivity.this.pending_switch_to = fragmentTag;
                    MainActivity.this.loginDialog();
                }
                setSupportProgressBarIndeterminateVisibility(false);
                mDrawerList.setItemChecked(MainActivity.this.getDefaultDrawerItem(), true);
            }
        });
    }
    
    private void showMap(){
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        Fragment mapFragment = this.fragments.get(MENU_MAP);
        transaction.replace(R.id.container, mapFragment);
        transaction.commit();
    }
    
    private int getDefaultDrawerItem() {
        return 0;
    }

    @Override
    protected void onStart() {
        Logger.i("MainActivity onStart");
        try {
            spiceManager.start(this);
        } catch (IllegalStateException e) {
            Logger.e("Failed to start spiceManager", e);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        try {
            spiceManager.shouldStop();
        } catch (IllegalStateException e){
            Logger.e("Failed to stop spiceManager", e);
        }
        super.onStop();
    }
    
    @Override
    protected void onDestroy(){
        this.getMyApplication().getAuth().deleteObserver(this);
        Logger.i("MainActivity onDestroy");
        super.onDestroy();
    }

    private void loginDialog(){
        LoginDialogFragment newFragment = new LoginDialogFragment();
        newFragment.show(getSupportFragmentManager(), "login");
    }

    public Application getMyApplication(){
        return (Application) super.getApplication();
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
        }
        switch (item.getItemId()) {
            case R.id.scan_barcode:
                switchTo(MENU_BARCODE);
                return true;
        };
        
        return super.onOptionsItemSelected(item);
    }

}
