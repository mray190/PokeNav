package net.michael_ray.macrobyte;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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

public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {

    private final boolean DEBUG = true;

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        setUpMapIfNeeded();
        return view;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState){
        super.onViewCreated(v, savedInstanceState);
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map)).getMap();
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(42.2928234, -83.7160523), 16));
            mMap.setPadding(0, 240, 0, 0);
            mMap.setOnMarkerClickListener(this);
            ((MacroByte)getActivity().getApplication()).map = mMap;
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            ((MacroByte)getActivity().getApplication()).map = null;
            Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.location_map);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove(mapFragment);
            ft.commit();
            mMap = null;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        PokemonInfoFragment fragment = new PokemonInfoFragment();
        Bundle data = new Bundle();
        data.putString("pokemon_data", marker.getSnippet());
        fragment.setArguments(data);
        ((MacroByte) getActivity().getApplication()).fragmentTransaction .replace(R.id.placeholder, fragment);
        ((MacroByte) getActivity().getApplication()).fragmentTransaction .addToBackStack(null);
        ((MacroByte) getActivity().getApplication()).fragmentTransaction .commit();
        return false;
    }
}