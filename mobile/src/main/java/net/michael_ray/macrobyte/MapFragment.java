package net.michael_ray.macrobyte;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        setUpMapIfNeeded();
        return view;
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map)).getMap();
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.2928234, -83.7160523), 16));
            mMap.setPadding(0, 120, 0, 0);
            ((MacroByte)getActivity().getApplication()).map = mMap;
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute("http://104.237.145.98:1030/pokemon");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            mMap = null;
            ((MacroByte)getActivity().getApplication()).map = null;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                return downloadContent(params[0]);
            } catch (IOException e) {
                return "Unable to retrieve data. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray array = json.getJSONArray("pokemon");
                LatLng poke_pos = null;
                for (int i=0; i<array.length(); i++) {
                    JSONObject pokemon = array.getJSONObject(i);
                    poke_pos = new LatLng(pokemon.getDouble("lat"), pokemon.getDouble("lon"));
                    MarkerOptions marker = new MarkerOptions().position(poke_pos).title(Integer.toString(pokemon.getInt("id")));
                    int resID = getResources().getIdentifier("poke_" + Integer.toString(pokemon.getInt("id")), "drawable", getActivity().getPackageName());
                    marker.icon(BitmapDescriptorFactory.fromResource(resID));
                    mMap.addMarker(marker);
                }
            } catch (Exception e) {
                Log.e("Pokemon", "Error", e);
            }
        }

        private String downloadContent(String myurl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
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
}