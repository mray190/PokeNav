package net.michael_ray.macrobyte.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Sam on 4/3/2016.
 */
public class ViewUtilities {

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    /**
     * Creates a bitmap version of the specified drawable.
     *
     * @param d Drawable to convert
     * @return A bitmap image of the supplied drawable.
     */
    public static Bitmap getBitmapFromDrawable(Drawable d) {
        return ((BitmapDrawable) d).getBitmap();
    }

    /**
     * Creates a bitmap from a provided view. Literally copied from the internet so I'm not
     * sure how it behaves
     * @param v The view from which to draw a bitmap
     * @return Returns the view as a bitmap image
     */
    public static Bitmap loadBitmapFromView(View v) {
        int width = v.getWidth();
        int height = v.getHeight();
        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

}
