package com.uit.instancesearch.camera.main;

import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ButtonRect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

public class MenuView extends View{

	private static final int CAMERA_BUTTON = 1;
	private static final int FLASH_BUTTON = 0;
	private static final int SELECT_BUTTON = 2;
	
	boolean enabled;
	
	final Bitmap cameraBm;
	final Bitmap cameraOnBm;
	final Bitmap flashBm;
	final Bitmap flashOnBm;
	final Bitmap selectBm;
	
	int clicked;
	boolean flashOn;
	
	final Paint paint;
	final int backColor;
	final int invBackColor;
	
	final int camPadding = 10;
	final int flashPadding = 20;
	final int selectPadding = 20;
	
	ButtonRect camButtonRect;
	ButtonRect flashButtonRect;
	ButtonRect selectButtomRect;
	
	final Context context;
	Canvas mCanvas;
	MainMenuListener listener;
	
	Drawable gradientLeft;
	
	public MenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		cameraBm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_camera);
		cameraOnBm = BitmapFactory.decodeResource(getResources(),R.drawable.ic_camera_on);
		flashBm = BitmapFactory.decodeResource(getResources(),R.drawable.ic_flash);
		flashOnBm = BitmapFactory.decodeResource(getResources(),R.drawable.ic_flash_on);
		selectBm = BitmapFactory.decodeResource(getResources(),R.drawable.ic_upload);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backColor = getResources().getColor(R.color.Transparent20);
		invBackColor = getResources().getColor(R.color.CyanTrans30);
		gradientLeft = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
        		new int[] {Color.argb (0x60, 0, 0, 0), Color.argb (0, 0, 0, 0) });
		
		clicked = -1;
		flashOn = false;
		mCanvas = null;
		
		enabled = true;
		
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (enabled) {
					int x = (int)e.getX();
					int y = (int)e.getY();
					int action = e.getAction();
					if (action == MotionEvent.ACTION_DOWN) {
						clicked  = getButton(x, y);
						invalidate();
					} else if (action == MotionEvent.ACTION_UP) {
						if (clicked == getButton(x, y)) {
							clickButton(clicked);
						}
						clicked = -1;
						invalidate();
					}
				}
				return true;
			}
		});
	}
	
	void initializeCameraListener(MainMenuListener l) {
		listener = l;
	}
	
	@SuppressLint("NewApi")
	public void hideMenu() {
		Interpolator a = new android.view.animation.OvershootInterpolator();
		this.animate().translationX(this.getWidth()).setDuration(1000).setInterpolator(a).start();
	}
	
	@SuppressLint("NewApi")
	public void showMenu() {
		Interpolator a = new android.view.animation.OvershootInterpolator();
		this.animate().translationX(0).setDuration(1000).setInterpolator(a).start();
	}	
	
	void initButton(int cw, int ch) {
		
		flashButtonRect = new ButtonRect(flashPadding, ch/6 - cw/2, cw - flashPadding, ch/6 - cw/2 + (cw - 2*flashPadding));
		camButtonRect = new ButtonRect(camPadding, (ch/2 - cw/2), cw - camPadding, (ch/2 - cw/2) + (cw - 2*camPadding));
		selectButtomRect = new ButtonRect(selectPadding, (5*ch/6 - cw/2), cw - selectPadding, (5*ch/6 - cw/2) + (cw - 2*selectPadding));
	}
	@Override
	public void onDraw(Canvas canvas) {
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		if (mCanvas == null) {
			initButton(w,h);
			mCanvas = canvas;
		}
		//paint.setColor(backColor);
		
		//Toast.makeText(context, w + "x" + h, Toast.LENGTH_LONG).show();
		
		drawCameraButton(canvas,w,h);
		drawFlashButton(canvas,w,h);
		drawSelectButton(canvas,w,h);
		
		//gradientLeft.setBounds(-15, 0, 0, canvas.getHeight());
		//gradientLeft.draw(canvas);
	}
	
	private void drawFlashButton(Canvas canvas, int cw, int ch) {
		Rect src = new Rect(0, 0, cameraBm.getWidth(), cameraBm.getHeight());
		canvas.drawBitmap(flashOn?flashOnBm:flashBm, src, flashButtonRect.getRect(), paint);
	}
	
	private void drawCameraButton(Canvas canvas, int cw, int ch) {
		Rect src = new Rect(0, 0, cameraBm.getWidth(), cameraBm.getHeight());
		canvas.drawBitmap(clicked==CAMERA_BUTTON?cameraOnBm:cameraBm, src, camButtonRect.getRect(), paint);	
	}
	
	private void drawSelectButton(Canvas canvas, int cw, int ch) {
//		paint.setColor(clicked == 2 ? invBackColor : backColor);
//		canvas.drawRect(0, ch - ch/3, cw, ch, paint);
		
		Rect src = new Rect(0, 0, cameraBm.getWidth(), cameraBm.getHeight());
		selectButtomRect.left = selectPadding;
		selectButtomRect.top = (int) (ch - ch/6 - cw/2);
		selectButtomRect.right = cw - selectPadding;
		selectButtomRect.bottom = selectButtomRect.top + (cw - 2*selectPadding);
		canvas.drawBitmap(selectBm, src, selectButtomRect.getRect(), paint);
		
	}
	
	private int getButton(int x, int y) {
		if (flashButtonRect.isContains(x, y)){
			return FLASH_BUTTON;
		} else if (camButtonRect.isContains(x, y)) {
			return CAMERA_BUTTON;
		} else if (selectButtomRect.isContains(x, y)) {
			return SELECT_BUTTON;
		}
		return -1;
	}
	
	private void clickButton(int button) {
		switch(button) {
		case FLASH_BUTTON: // flash
			flashOn = !flashOn;
			listener.onCameraFlashChange(flashOn);
			break;
		case CAMERA_BUTTON: // capture
			listener.onCameraCapture();
			setTouchEnabled(false);
			break;
		case SELECT_BUTTON: // select image
			listener.onSelectImage();
			break;
		default: break;
		}
	}
	
	public void setTouchEnabled(boolean b) {
		enabled = b;
	}
	
}
