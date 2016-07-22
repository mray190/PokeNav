package net.michael_ray.macrobyte;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

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
}