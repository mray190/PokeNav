package net.michael_ray.macrobyte.Widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import net.michael_ray.macrobyte.Utilities.ViewUtilities;

/**
 * Created by Sam on 1/14/2016.
 * This will eventually represent a more advanced color picker than the previous one, with
 * a large amount of color possibilities.
 */
public class AdvancedColorPicker extends View {
    private Paint mLinePaint;
    private Paint mFillPaint;
    private Paint mCenterPaint;
    private Paint mSelectedPaint;
    private int mHeight, mWidth;
    private float mCenterX, mCenterY, mFingerX, mFingerY;
    private float mSelectionRad = 10.f;
    private float mRadiusOuter;
    private RectF mRectOuter;
    private boolean mMeasured;
    private OnColorChangedListener mListener;
    SweepGradient mSweepShader;
    RadialGradient mGradShader;
    Bitmap mPickerMap;
    private Shader mComposeShader;

    public AdvancedColorPicker(Context c) {
        super(c);
        initialize(c);
    }

    public AdvancedColorPicker(Context c, AttributeSet attrs) {
        super(c, attrs);
        initialize(c);
    }

    public AdvancedColorPicker(Context c, AttributeSet attrs, int defStyleAttr) {
        super(c, attrs, defStyleAttr);
        initialize(c);
    }

    private void initialize(Context c) {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(2.f);
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setStyle(Paint.Style.FILL);
        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setColor(Color.WHITE);
        mCenterPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint = new Paint();
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setColor(0xff000000);
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setStrokeWidth(6.f);
        mMeasured = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // if this is the first time we draw, let's get the dimensions of the view
        if (!mMeasured) {
            this.mHeight = this.getHeight();
            this.mWidth = this.getWidth();
            this.mCenterX = this.mWidth / 2;
            this.mCenterY = this.mHeight / 2;
            // make a rectangle so that our view is a circle and not an oval
            if (this.mWidth > this.mHeight) {
                this.mRadiusOuter = this.mHeight / 2;
                mRadiusOuter *= .98;
                this.mRectOuter = new RectF(this.mCenterX - this.mRadiusOuter, this.mCenterY - this.mRadiusOuter, this.mCenterX + this.mRadiusOuter, this.mCenterY + this.mRadiusOuter);
                mGradShader = new RadialGradient(this.mCenterX, this.mCenterY, this.mRadiusOuter, 0x22FFFFFF, Color.WHITE, Shader.TileMode.MIRROR);
            } else {
                this.mRadiusOuter = this.mWidth / 2;
                mRadiusOuter *= .98;
                this.mRectOuter = new RectF(this.mCenterX - this.mRadiusOuter, this.mCenterY - this.mRadiusOuter, this.mCenterX + this.mRadiusOuter, this.mCenterY + this.mRadiusOuter);
                mGradShader = new RadialGradient(this.mCenterX, this.mCenterY, this.mRadiusOuter, 0x22FFFFFF, Color.WHITE, Shader.TileMode.MIRROR);
            }
            // create the array of colors that we'll sweep from
            int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.RED};
            float[] positions = {0, .33f, .66f, 1.f};
            // make our sweep shader
            mSweepShader = new SweepGradient(mCenterX, mCenterY, colors, positions);
            // compose the two shaders
            mComposeShader = new ComposeShader(mSweepShader, mGradShader, new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mFillPaint.setShader(mComposeShader);

            this.mMeasured = true;
        }
        canvas.drawArc(mRectOuter, 0, 360, true, mFillPaint);
        canvas.drawArc(new RectF(mFingerX - mSelectionRad, mFingerY - mSelectionRad, mFingerX + mSelectionRad, mFingerY + mSelectionRad), 0, 360, false, mLinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mPickerMap == null) {
            mPickerMap = ViewUtilities.loadBitmapFromView(this);
        }
        // See if the user selected a new color on a touch start
        if (e.getActionMasked() == MotionEvent.ACTION_MOVE || e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            float xLoc = e.getX();
            float yLoc = e.getY();
            // clamp to within the circle
            double radius = Math.sqrt(Math.pow(Math.abs(xLoc - mCenterX), 2) + Math.pow(Math.abs(yLoc - mCenterY), 2));
            if (radius > mRadiusOuter){
                double mult = mRadiusOuter / (radius + 1.f); // adding 1.f for roudoff errors
                xLoc = (float) (mCenterX + (xLoc - mCenterX) * mult);
                yLoc = (float) (mCenterY + (yLoc - mCenterY) * mult);
            }
//            xLoc = Math.min(Math.max(xLoc, mCenterX - mRadiusOuter), mCenterX + mRadiusOuter);
//            yLoc = Math.min(Math.max(yLoc, mCenterY - mRadiusOuter), mCenterY + mRadiusOuter);
            mFingerX = xLoc;
            mFingerY = yLoc;
            int pixel = mPickerMap.getPixel((int) xLoc, (int) yLoc);
            if (mListener != null) {
                // we want to translate our alpha color to a solid one for now
                double mul = (double) (0xFF - Color.alpha(pixel)) / 0xFF;
                int red = (int) ((0xFF - Color.red(pixel)) * mul + Color.red(pixel));
                int green = (int) ((0xFF - Color.green(pixel)) * mul + Color.green(pixel));
                int blue = (int) ((0xFF - Color.blue(pixel)) * mul + Color.blue(pixel));
                int newColor = Color.rgb(red, green, blue);
                mListener.onColorChanged(newColor);
            }
            this.invalidate();
        }
        return true;
    }

    /**
     * Sets the OnColorChanged listener for the color picker, which is told when
     * the selected color changes.
     *
     * @param listener
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        this.mListener = listener;
        //mListener.onColorChanged(this.getSolidColor());
    }

    /**
     * Created by sschweba on 6/17/2015.
     */
    public interface OnColorChangedListener {

        /**
         * Called when a new color is selected on the color wheel
         *
         * @param color
         *            the rgb value of the color selected with alpha
         */
        public void onColorChanged(int color);

        public void onBackgroundColorChanged(int color);
    }
}
