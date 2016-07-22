package net.michael_ray.macrobyte;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.michael_ray.macrobyte.Utilities.PokemonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Sam on 7/21/2016.
 */
public class PokemonInfoFragment extends Fragment {
    // Constants for parsing JSON
    private static final String POKE_DATA_STRING = "pokemon_data";
    private static final String POKE_DATA_GOAWAY = "goaway";
    private static final String POKE_DATA_LAT = "lat";
    private static final String POKE_DATA_LONG = "lon";
    private static final String POKE_DATA_ID = "id";

    private TextView mPokemonNameText;
    private TextView mPokemonTimerText;
    private TextView mPokemonTypeText;
    private TextView mPokemonEvoText;
    private TextView mPokemonDistanceText;
    private ImageView mPokemonImage;

    private String mSavedData;
    private long mRemainingTime;
    private double mLat;
    private double mLong;
    private int mID;

    private Timer mTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSavedData = getArguments().getString(POKE_DATA_STRING);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_poke_info, container, false);
        // get all our views
        mPokemonNameText = (TextView) view.findViewById(R.id.text_poke_info_name);
        mPokemonTimerText = (TextView) view.findViewById(R.id.text_poke_info_timer);
        mPokemonTypeText = (TextView) view.findViewById(R.id.text_poke_info_type);
        mPokemonEvoText = (TextView) view.findViewById(R.id.text_poke_info_evolve);
        mPokemonDistanceText = (TextView) view.findViewById(R.id.text_poke_info_distance);
        mPokemonImage = (ImageView) view.findViewById(R.id.image_poke_info_image);

        // Get the bundle pokemon data
        // It will be structured like this:
        // {"lat": 30.24582810408313, "goaway": 1469149571805.867, "lon": -97.84057751302967, "id": 21}
        try{
            JSONObject dataJSON = new JSONObject(mSavedData);
            mRemainingTime = dataJSON.getLong(POKE_DATA_GOAWAY);
            mLat = dataJSON.getDouble(POKE_DATA_LAT);
            mLong = dataJSON.getDouble(POKE_DATA_LONG);
            mID = dataJSON.getInt(POKE_DATA_ID);

        } catch (JSONException e){
            // This should never happen
            mRemainingTime = 0;
            mLat = 0.0;
            mLong = 0.0;
            mID = 1;
        }
        // Using the data, set all the views
        mPokemonNameText.setText(PokemonUtils.getPokemonName(this.getActivity(), mID));
        updateRemainingtime();
        // TODO format remaining time string
        updateRemainingTimeText();
        // TODO
        double distance = 0.0;
        Location currLocation = new Location(((MacroByte) this.getActivity().getApplication()).myLocation);
        currLocation.setLatitude(mLat);
        currLocation.setLongitude(mLong);
        mPokemonDistanceText.setText(((MacroByte) this.getActivity().getApplication()).myLocation.distanceTo(currLocation) + " meters away");
        //distance = MapUtilities.distance()
        // update from miles to feet
        distance = distance * 5280;
        mPokemonDistanceText.setText(distance + " feet away");
        mPokemonTypeText.setText("Type: " + PokemonUtils.getPokemonType(this.getActivity(), mID));
        mPokemonEvoText.setText("Evolves into: " + PokemonUtils.getPokemonEvo(this.getActivity(), mID));
        int resID = getResources().getIdentifier("poke_" + Integer.toString(mID), "drawable", getActivity().getPackageName());
        mPokemonImage.setImageResource(resID);
        // Once we're done setting everything, start a timer for the remaining time
        this.mTimer = new Timer();
        this.mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PokemonInfoFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateRemainingtime();
                        updateRemainingTimeText();
                    }
                });
            }
        }, 0, 1000);
        return view;
    }

    /**
     * Gets the time remaining on the pokemon's appearance
     * @param goAwayTime The go away time, in milliseconds
     * @return The remaining time, in milliseconds
     */
    private static long getRemainingTime(long goAwayTime){
        long currTime = System.currentTimeMillis();
        if (currTime > goAwayTime) {
            return 0;
        }
        return (goAwayTime - currTime);
    }

    /**
     * Updates the remaining time
     */
    private void updateRemainingtime(){
        long currTime = System.currentTimeMillis();
        if (currTime > mRemainingTime) {
            mRemainingTime = 0;
        }
        mRemainingTime =  (mRemainingTime - currTime);
    }

    /**
     * Updates the remaining time
     */
    private void updateRemainingTimeText(){
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        mPokemonTimerText.setText(sdf.format(new Date(mRemainingTime)) + " Remaining");
    }
}
