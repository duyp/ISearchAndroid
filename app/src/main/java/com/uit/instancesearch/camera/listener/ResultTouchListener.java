package com.uit.instancesearch.camera.listener;


import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class ResultTouchListener implements OnTouchListener{

	Point pressedPoint;
	boolean pressed;
	
	ResultListener listener;
	
	public ResultTouchListener(ResultListener l) {
		this.listener = l;
		pressedPoint = new Point();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent e) {
		int act = e.getAction();
		int x = (int)e.getX();
		int y = (int)e.getY();
		switch(act) {
		case MotionEvent.ACTION_DOWN:
				pressedPoint.x = x;
				pressedPoint.y = y;
				pressed = true;
				listener.onResultViewPrepareResize();
			break;
		case MotionEvent.ACTION_MOVE:
			if (pressed) {
				int dx = pressedPoint.x - x;
				Log.d("debug", "x: " + x);
				listener.onResultViewResize(dx);
			}
			break;
		case MotionEvent.ACTION_UP:
			pressed = false;
			listener.onResultViewResized();
			break;
		default: break;
		}
		return true;
	}

	
}
