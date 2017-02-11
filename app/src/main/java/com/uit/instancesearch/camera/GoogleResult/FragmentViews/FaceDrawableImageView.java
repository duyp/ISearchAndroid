package com.uit.instancesearch.camera.GoogleResult.FragmentViews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;

import com.uit.instancesearch.camera.GoogleModels.FaceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 25/01/2017.
 */

public class FaceDrawableImageView extends DrawableImageView {

    private ArrayList<List<Point>> inFacePoints;
    private ArrayList<List<float[]>> landmarkPoints;

    public FaceDrawableImageView(Context context) {
        super(context);
    }

    public FaceDrawableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceDrawableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        super.init();
        inFacePoints = new ArrayList<>();
        landmarkPoints = new ArrayList<>();
    }

    public void addFace(FaceItem item) {
        super.addRectangle(item.outFacePoints);
        inFacePoints.add(item.inFacePoints);
        landmarkPoints.add(item.componentPoints);
    }

    public void drawFaces() {
        super.drawRectangles(COLOR_DEFAULT, RECT_DEFAULT_STROKE);
        invalidate();
    }

    @Override
    public void setHighlightIndex(int index) {
        super.setHighlightIndex(index);
    }

    @Override
    public void onDraw(Canvas mCanvas) {
        super.onDraw(mCanvas);
        drawListPoints(mCanvas,validatePoints(inFacePoints));
    }
}
