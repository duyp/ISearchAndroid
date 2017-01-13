package com.uit.instancesearch.camera.listener;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;

public interface RegionSelectListener {
	public void onRegionSelected(Rect regionRect, MotionEvent e);
	public void onRegionConfirmed(Bitmap croppedImage);
	public void onRegionCancelScan();
}
