package com.uit.instancesearch.camera.GoogleResult.FragmentViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.uit.instancesearch.camera.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 20/01/2017.
 */

public class DrawableImageView extends ImageView {

    public static final int COLOR_DEFAULT = -1;
    public static final String TAG_DEBUG_IMAGEVIEW = "TAG_DEBUG_IMAGEVIEW";

    public static final float RECT_DEFAULT_STROKE = 3.0f;
    public static final float RECT_MIN_STROKE = 1.0f;

    protected Paint paint;
    protected Paint hlPaint; // highlight paint
    protected int lineColor;
    protected int hlcolor;
    private boolean draw = false;

    private ArrayList<List<Point>> pointsList;

    private int x = 0; // X coordinate of image inside image view
    private int y = 0; // Y coordinate of image inside image view

    private float scaleX; // scale value of real image and display image in imageview
    private float scaleY; //

    private int highlightIndex;

    public DrawableImageView(Context context) {
        super(context);
        init();
    }

    public DrawableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        if(Build.VERSION.SDK_INT >= 23) {
            lineColor = ContextCompat.getColor(this.getContext(), R.color.blueColor100);
            hlcolor = ContextCompat.getColor(getContext(), R.color.red);
        } else {
            lineColor = getResources().getColor(R.color.blueColor100);
            hlcolor = getResources().getColor(R.color.red);
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hlPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(lineColor);
        hlPaint.setColor(hlcolor);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        hlPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        pointsList = new ArrayList<>();

        highlightIndex = -1;
    }

    // get a rectangle from 4 points
    public void addRectangle(List<Point> points) {
        pointsList.add(points);
    }

    public void drawRectangles(int color, float stroke) {
        if (color != COLOR_DEFAULT)  paint.setColor(color);
        paint.setStrokeWidth(stroke);
        hlPaint.setStrokeWidth(RECT_DEFAULT_STROKE);
        draw = true;
        invalidate();
    }

    public void setHighlightIndex(int index) {
        highlightIndex = index;
        invalidate();
    }

    protected ArrayList<List<Point>> validatePoints(ArrayList<List<Point>> inputPoints) {
        try {
            float[] scales = getBitmapScaleInImageView(this);
            scaleX = scales[0];
            scaleY = scales[1];
//            Log.d(TAG_DEBUG_IMAGEVIEW, "Scale value: " + scaleX + "-" + scaleY);
//            Log.d(TAG_DEBUG_IMAGEVIEW, "Scale value: " + scaleX + "-" + scaleY);
            ArrayList<List<Point>> pointsListScaled = new ArrayList<>();
            if (inputPoints != null && scaleX > 0 && scaleY > 0) {
                for (List<Point> points : inputPoints) {
                    List<Point> pTemp = new ArrayList<>();
                    for (Point p : points) {
                        pTemp.add(new Point((int) (p.x * scaleX), (int) (p.y * scaleY)));
                    }
                    pointsListScaled.add(pTemp);
                }
                return pointsListScaled;
            }
            else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void drawListPoints(Canvas mCanvas, ArrayList<List<Point>> listPoints) {
        int[] rect = getBitmapPositionInsideImageView(this);
        x = rect[0];
        y = rect[1];
//        x = 0; y = 0;
        int n;
        if (listPoints != null && (n = listPoints.size()) > 0) {
            for(int k = 0; k < n; k++) {
                List<Point> points = listPoints.get(k);
                int n1 = points.size(); //4
                for(int i = 0; i < n1; i++) {
                    int end = i + 1;
                    if (end == n1) end = 0;
                    mCanvas.drawLine(points.get(i).x + x, points.get(i).y + y,
                            points.get(end).x + x, points.get(end).y + y,
                            k == highlightIndex ? hlPaint : paint);
                }
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (draw) {
            drawListPoints(canvas, validatePoints(pointsList));
        }
    }

    /**
    * Return the scale values of Bitmap inside an imageView
    * @param imageView source ImageView
    * @return 0: scaleX, 1: scaleY
    */
    public static float[] getBitmapScaleInImageView(ImageView imageView) {
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);
        float[] scales = new float[2];
        scales[0] = f[Matrix.MSCALE_X];
        scales[1] = f[Matrix.MSCALE_Y];
        return scales;
    }
    /**
     * Returns the bitmap position inside an imageView.
     * @param imageView source ImageView
     * @return 0: left, 1: top, 2: width, 3: height
     */
    public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }
}
