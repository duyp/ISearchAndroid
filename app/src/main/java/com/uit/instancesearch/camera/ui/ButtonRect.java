package com.uit.instancesearch.camera.ui;

import android.graphics.Rect;

public class ButtonRect {
	public float left;
	public float right;
	public float top;
	public float bottom;
	
	public ButtonRect() {
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;
	}
	
	public ButtonRect(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public void set(float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public boolean isContains(float x, float y) {
		return (x >= left && x <= right && y >= top && y <= bottom);
	}
	
	public Rect getRect() {
		return new Rect((int)left, (int)top, (int)right, (int)bottom);
	}
}