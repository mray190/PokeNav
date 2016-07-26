package net.michael_ray.macrobyte;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOCATION_PERMISSION_IDENTIFIER = 0;
    private Messenger bgMessenger;
    private boolean bgBound;
    public static FragmentManager fragmentManager;
    public Fragment currFragment;
    private SharedPreferences sharedPref;

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MapFragment fragment = new MapFragment();
        fragmentTransaction.add(R.id.placeholder, fragment);
        fragmentTransaction.isAddToBackStackAllowed();
        fragmentTransaction.commit();
        currFragment = fragment;

        currFragment = fragmentManager.findFragmentById(R.id.placeholder);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        setStatusBarTranslucent(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!hasLocationPermissions()) requestLocationPermissionsBeforeStart();
        if (!locationSettingsEnabled()) startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        ((MacroByte) getApplication()).startLocationUpdates();
        ((MacroByte) getApplication()).fragmentTransaction = fragmentManager.beginTransaction();
        bindService(new Intent(this, BackgroundTasks.class), bgConnection, Context.BIND_AUTO_CREATE);
    }

    public void refresh_pokemon(View view) {
        Location myLoc = ((MacroByte)getApplication()).myLocation;
        if (myLoc!=null) {
            DownloadTask downloadTask = new DownloadTask();
            String lat = Double.toString(myLoc.getLatitude());
            String lon = Double.toString(myLoc.getLongitude());
            String alt = Double.toString(myLoc.getAltitude());
            Button button = (Button)findViewById(R.id.refresh_pokemon);
            button.setEnabled(false);
            Toast.makeText(this, "Refreshing Pokemon", Toast.LENGTH_SHORT).show();
            downloadTask.execute("http://104.237.145.98:1030/pokemon", lat, lon, alt);
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                return downloadContent(params[0], params[1], params[2], params[3]);
            } catch (IOException e) {
                return "Unable to retrieve data. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            ((MacroByte) getApplication()).markers.clear();
            try {
                Log.d("Pokemon", result);
                JSONObject json = new JSONObject(result);
                JSONArray array = json.getJSONArray("pokemon");
                LatLng poke_pos = null;
                for (int i=0; i<array.length(); i++) {
                    JSONObject pokemon = array.getJSONObject(i);
                    Log.d("Pokemon", pokemon.toString());
                    long time = (long)pokemon.getDouble("goaway")*1000;
                    long now_time = System.currentTimeMillis();
                    if ((time-now_time)>0) {
                        poke_pos = new LatLng(pokemon.getDouble("lat"), pokemon.getDouble("lon"));
                        MarkerOptions markerOptions = new MarkerOptions().position(poke_pos);
                        int resID = getResources().getIdentifier("poke_" + Integer.toString(pokemon.getInt("id")), "drawable", getPackageName());
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(resID));
                        markerOptions.snippet(pokemon.toString());
                        Marker marker = ((MacroByte) getApplication()).map.addMarker(markerOptions);
                        ((MacroByte) getApplication()).markers.add(marker);
                        Intent intentAlarm = new Intent(Home.this, AlarmReceiver.class);
                        intentAlarm.putExtra("marker_id", ((MacroByte) getApplication()).markers.size() - 1);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        alarmManager.set(AlarmManager.RTC_WAKEUP, (long)pokemon.getDouble("goaway"), PendingIntent.getBroadcast(Home.this, 1,
                                intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
                    }
                }
            } catch (Exception e) {
                Log.e("Pokemon", "Error", e);
            }
            Button button = (Button)findViewById(R.id.refresh_pokemon);
            button.setEnabled(true);
        }

        private String downloadContent(String myurl, String lat, String lon, String alt) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                String out_query = "lat=" + lat + "&lon=" + lon + "&alt=" + alt;
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(out_query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                is = conn.getInputStream();
                String contentAsString = convertInputStreamToString(is);
                return contentAsString;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String convertInputStreamToString(InputStream stream) throws IOException, UnsupportedEncodingException {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            return total.toString();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((MacroByte)getApplication()).setCurrActivity(this);
    }

    @Override
    protected void onPause() {
        ((MacroByte)getApplication()).setCurrActivity(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ((MacroByte)getApplication()).stopLocationUpdates();
        if (bgBound) {
            unbindService(bgConnection);
            bgBound = false;
        }
        super.onDestroy();
    }

    private ServiceConnection bgConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            bgMessenger = new Messenger(service);
            bgBound = true;
        }
        public void onServiceDisconnected(ComponentName className) {
            bgMessenger = null;
            bgBound = false;
        }
    };

    private boolean hasLocationPermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    private void requestLocationPermissionsBeforeStart() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_IDENTIFIER
        );
    }

    private boolean locationSettingsEnabled() {
        if (Build.VERSION.SDK_INT >= 19) {
            try {
                int locationMode = Settings.Secure.getInt(
                        getContentResolver(),
                        Settings.Secure.LOCATION_MODE
                );
                return locationMode != 0;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        } else {
            String locationProviders = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            );
            return !(locationProviders == null || locationProviders.equals(""));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.live_view) {
//            MapsFragment fragment = new MapsFragment();
//            fragmentTransaction.replace(R.id.placeholder, fragment);
//            fragmentTransaction.addToBackStack(null);
//            fragmentTransaction.commit();
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
