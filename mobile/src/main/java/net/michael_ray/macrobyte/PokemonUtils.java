package net.michael_ray.macrobyte;

import android.content.Context;
import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Sam on 7/21/2016.
 * Utility class for pokemon-related inquiries. The main purpose initially is to allow us to
 * look up information given a pokemon number
 */
public class PokemonUtils {
    private static final String POKEMON_NAME_KEY = "name";
    private static final String POKEMON_TYPE_KEY = "type";

    /**
     * Given a pokedex entry number, returns the name of that pokemon.
     *
     * @param c      The activity context to enable opening of files
     * @param pokeId The pokedex id number
     * @return Returns the name of the pokemon.
     */
    public static String getPokemonName(Context c, int pokeId) {
        try {
            return getPokemonJson(c, pokeId).getString(POKEMON_NAME_KEY);
        } catch (JSONException e) {
            return "Unknown";
        } catch (Exception e){
            return "Unknown";
        }
    }

    /**
     * Given a pokedex entry number, returns the type of that pokemon
     * @param c The activity context to enable opening of files
     * @param pokeId The pokedex id number of the pokemon
     * @return Returns the type of the pokemon.
     */
    public static String getPokemonType(Context c, int pokeId){
        try {
            return getPokemonJson(c, pokeId).getString(POKEMON_TYPE_KEY);
        } catch (JSONException e){
            return "Unknown";
        }
    }

    /**
     * Helper method to get the pokemon json object for use in other methods
     *
     * @param c          The activity context to open files
     * @param pokemonNum The number of the pokemon
     * @return Returns a jsonobject of that pokemon
     */
    private static JSONObject getPokemonJson(Context c, int pokemonNum) {
        try {
            InputStream is = c.getAssets().open("pokemon_list.json");
            JsonReader reader = new JsonReader(new InputStreamReader(is));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject jsonObj = new JSONObject(json);
            return jsonObj.getJSONObject("" + pokemonNum);
        } catch (JSONException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
