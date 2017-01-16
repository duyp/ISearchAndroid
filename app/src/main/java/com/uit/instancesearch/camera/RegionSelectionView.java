package com.uit.instancesearch.camera;

import com.uit.instancesearch.camera.listener.RegionSelectListener;
import com.uit.instancesearch.camera.manager.CameraManager;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class RegionSelectionView extends View {

	static final int MIN_WIDTH = 100;
	static final int MIN_HEIGHT = 100;
	static final int DEFAULT_SIZE = 400;

	static final int RIGHT_TO_LEFT = 0;
	static final int LEFT_TO_RIGHT = 1;
	
	static final float RECT_STROKE = 5.0f;

	public static Drawable GRADIENT_RL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
													new int[]{Color.argb(0, 0, 255, 255), Color.argb(0x60, 0, 255, 255)});
	public static Drawable GRADIENT_LR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
									  new int[]{Color.argb(0, 0, 255, 255), Color.argb(0x60, 0, 255, 255)});


	private Rect regionRect;
	
	private final Paint paint;
	private final int backColor;
	private final int lineColor;
	final PorterDuffXfermode clearFermode;
	final PorterDuffXfermode srcFermode;
	
	final Bitmap closeBm;
	final Rect closeBmRect;
	ButtonRect closeButtonRect;
	
	private boolean selected;
	private boolean pressed;
	Point pressedPoint;
	
	private boolean selectEnabled;
	boolean closeEnabled;
	
	int widthLimited;
	
	private RegionSelectListener listener;
	
	AsyncTaskScanner scanner;
	
	Context context;
	
	public RegionSelectionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		// Initialize these once for performance rather than calling them every time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backColor = getResources().getColor(R.color.Transparent60);
		lineColor = getResources().getColor(R.color.blueColor100);
		
		closeEnabled = false;
		closeBm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_close);
		closeBmRect = new Rect(0, 0, closeBm.getWidth(), closeBm.getHeight());
		closeButtonRect = new ButtonRect(0, 0, 0, 0);
		
		clearFermode = new PorterDuffXfermode(Mode.CLEAR);
		srcFermode = new PorterDuffXfermode(Mode.SRC);
	}
	
	public void init(RegionSelectListener l) {
		
		regionRect 		= null;
		scanner			= null;
		selected		= false;
		pressed 		= false;
		scanning		= false;
		selectEnabled 	= true;
		listener 		= l;
		pressedPoint 	= new Point();
		
		this.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				if (selectEnabled) {
					int action = me.getAction();
					if(action == MotionEvent.ACTION_DOWN) { // pressed
						if (closeButtonRect.isContains(me.getX(), me.getY())) {
							closeButtonClicked();
						} else {
							touchPressed(me);
						}
					} else if (action == MotionEvent.ACTION_MOVE){ // drag
						touchMoving(me);
					} else if (action == MotionEvent.ACTION_UP) { // released
						touchReleased(me);
					}
				} else if (closeButtonRect.isContains(me.getX(), me.getY())) {
					closeButtonClicked();
				}
				return true;
			}
		});
	}
	
	
	public void setWidthLimited(int w) {
		widthLimited = w;
	}

	public void setRegion(Rect region) {
		regionRect = region;
		selected = true;
		selectEnabled = false;
		invalidate();
	}
	
	public Rect getRegion() {
		return regionRect;
	}
	
	public void setSelectEnabled(boolean enabled) {
		selectEnabled = enabled;
	}


	Rect scanRect;
	int scanDirection;

	public void setScanRect(int direction, int p1x, int p1y, int p2x, int p2y) {
		scanRect = new Rect(p1x,p1y,p2x,p2y);
		scanDirection = direction;
		invalidate();
	}
	@Override
	public void onDraw(Canvas canvas) {
		if (regionRect == null) {
			regionRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
		
		if (selected) {
			//int width = widthLimited;
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			
			validateRect();
			paint.setColor(backColor);
			canvas.drawRect(0, 0, width, regionRect.top, paint);
			canvas.drawRect(0, regionRect.top, regionRect.left, regionRect.bottom, paint);
			canvas.drawRect(regionRect.right, regionRect.top, width, regionRect.bottom, paint);
			canvas.drawRect(0, regionRect.bottom, width, height, paint);
			
			paint.setColor(lineColor);
			paint.setStrokeWidth(RECT_STROKE);
			canvas.drawLine(regionRect.left, regionRect.top, regionRect.right, regionRect.top, paint);
			canvas.drawLine(regionRect.left, regionRect.bottom, regionRect.right, regionRect.bottom, paint);
			canvas.drawLine(regionRect.left, regionRect.top, regionRect.left, regionRect.bottom, paint);
			canvas.drawLine(regionRect.right, regionRect.top, regionRect.right, regionRect.bottom, paint);

		}
		
		if (selected && closeEnabled) {
			//drawCloseButton(canvas);
		}
		
		if (scanning && scanRect!=null) {
			if (scanDirection == RIGHT_TO_LEFT) { // right to left
				GRADIENT_RL.setBounds(scanRect.left,scanRect.top,scanRect.right,scanRect.bottom);
				GRADIENT_RL.draw(canvas);
			} else {
				GRADIENT_LR.setBounds(scanRect.left,scanRect.top,scanRect.right,scanRect.bottom);
				GRADIENT_LR.draw(canvas);
			}


//			paint.setColor(Color.WHITE);
//			int h = CameraManager.getScreenSize().y;
//			int w = CameraManager.getScreenSize().x;
//			paint.setTextSize(h / 15);
//			canvas.drawText("Analyzing...", h/30, canvas.getHeight() - h/30, paint);
//			
//			canvas.drawText("Cancel", canvas.getWidth() - w/5 - h/30, canvas.getHeight() - h/25, paint);
//			closeButtonRect.set(canvas.getWidth() - w/10, canvas.getHeight() - h/10, canvas.getWidth() - h/30, canvas.getHeight() - h/50);
//			canvas.drawBitmap(closeBm, closeBmRect, closeButtonRect.getRect(), paint);
		}
		
		if(!selected && !scanning) {
			paint.setXfermode(clearFermode);
			canvas.drawPaint(paint);
			paint.setXfermode(srcFermode);
		}
	}
	
	void drawCloseButton(Canvas c) {
		closeButtonRect.set(regionRect.right - 103, regionRect.top + 3, regionRect.right - 3, regionRect.top + 100);
		c.drawBitmap(closeBm, closeBmRect, closeButtonRect.getRect(), paint);
	}
	
	void closeButtonClicked() {
		if (scanning) {
			selectEnabled = true;
			stopScan();
			listener.onRegionCancelScan();
		} else {
			closeRegion();
		}
	}
	
	void closeRegion() {
		selected = false;
		regionRect.set(0, 0, this.getWidth(), this.getHeight());
		invalidate();
	}
	
	
	void validateRect() {
		int temp;
		if (regionRect.left > regionRect.right) {
			temp = regionRect.left;
			regionRect.left = regionRect.right;
			regionRect.right = temp;
		}
		if (regionRect.top > regionRect.bottom) {
			temp = regionRect.top;
			regionRect.top = regionRect.bottom;
			regionRect.bottom = temp;
		}
	}
	
	boolean scanning;
	
	public boolean isScanning() {
		return scanning;
	}
	
	public boolean isRegionSelected() {
		return selected;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void startScan() {
		//showAnalyzingDialog();
		scanner = new AsyncTaskScanner();
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			scanner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			scanner.execute();
		}
		
		closeEnabled = false;
		scanning = true;
	}
	
	public void stopScan() {
		if (scanner != null) {
			scanner.stopScan();
			closeRegion();
			closeEnabled = false;
			selectEnabled = true;
			scanning = false;
			listener.onRegionCancelScan();
		}
	}
	
	public void doneScan() {
		if (scanner != null) {
			scanner.stopScan();
			closeRegion();
			scanning = false;
		}
	}
	
	private class AsyncTaskScanner extends AsyncTask<Void, Void, Void> {

		static final int JUMP = 20; // determine scanning speed
		static final int SLEEP_TIME = 30; // ms
		static final int LINE_WIDTH = 100;
		
		Point p1;
		Point p2;
		
		boolean stop;
		int direction;

		Drawable rlGradient;
		Drawable lrGradient;

		public AsyncTaskScanner() {
			if (regionRect != null) {
				p1 = new Point(regionRect.left, regionRect.top);
				p2 = new Point(regionRect.left, regionRect.bottom);
				stop = false;
				direction = RIGHT_TO_LEFT;

				rlGradient = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
						new int[]{Color.argb(0, 0, 255, 255), Color.argb(0x60, 0, 255, 255)});
				lrGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
						new int[]{Color.argb(0, 0, 255, 255), Color.argb(0x60, 0, 255, 255)});
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			//mCanvas.save();
			while(!stop) {
				if (direction == RIGHT_TO_LEFT) { // right to left
					if (p1.x - JUMP < regionRect.left) {
						p1.x = regionRect.left;
						p2.x = regionRect.left;
						direction = LEFT_TO_RIGHT;
					} else {
						p1.x = p1.x - JUMP;
						p2.x = p2.x - JUMP;
					}
				} else {			// left to right
					if (p1.x + JUMP > regionRect.right) {
						p1.x = regionRect.right;
						p2.x = regionRect.right;
						direction = RIGHT_TO_LEFT;
					} else {
						p1.x = p1.x + JUMP;
						p2.x = p2.x + JUMP;
					}
				}
			publishProgress(new Void[] {null});
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void...params) {
			if (!stop) {
				if (direction == RIGHT_TO_LEFT) {
					int x;
					if (p2.x + LINE_WIDTH > regionRect.right) {
						x = regionRect.right;
					} else {
						x = p2.x + LINE_WIDTH;
					}
					setScanRect(direction, p1.x, p1.y, x, p2.y);
				    //rlGradient.setBounds(p1.x, p1.y, x, p2.y);
					//Log.w("SCANNER", lrGradient.getBounds().toString());
				    //rlGradient.draw (mCanvas);
				} else {
					int x;
					if (p1.x - LINE_WIDTH < regionRect.left) {
						x = regionRect.left;
					} else {
						x = p1.x - LINE_WIDTH;
					}
					setScanRect(direction, x, p1.y, p2.x, p2.y);
					//lrGradient.setBounds(x, p1.y, p2.x, p2.y);
					//Log.w("SCANNER", lrGradient.getBounds().toString());
					//lrGradient.draw (mCanvas);
				}
			}
			invalidate();
		}
		
		public void stopScan() {
			stop = true;
			//mCanvas.restore();
			invalidate();
		}
	}
	
	private void touchPressed(MotionEvent me) {
		int x = (int)me.getX();
		int y = (int)me.getY();
		if (x > widthLimited || y > CameraManager.getScreenSize().y) { //x > widthLimited
			return;
		}
		pressed = true;
		closeEnabled = false;
		if (regionRect == null) {
			regionRect = new Rect();
		}
		regionRect.left = x;
		regionRect.top = y;
		
		// save x,y to pressed point
		pressedPoint.x = x;
		pressedPoint.y = y;
	}
	
	private void touchReleased(MotionEvent me) {
		Point screenSize = CameraManager.getScreenSize();
		if (pressed) {
			if (regionRect.right - regionRect.left < MIN_WIDTH &&
				regionRect.bottom - regionRect.top < MIN_HEIGHT) { // selected region < min
				int nLeft = regionRect.left - DEFAULT_SIZE/2;
				int nTop = regionRect.top - DEFAULT_SIZE/2;
				if (nLeft < 0) {
					regionRect.left = 0;
					regionRect.right = DEFAULT_SIZE;
				} else {
					int nRight = nLeft + DEFAULT_SIZE;
					if (nRight > widthLimited) {
						regionRect.right = widthLimited;
						regionRect.left = regionRect.right - DEFAULT_SIZE;
					} else {
						regionRect.left = nLeft;
						regionRect.right = nLeft + DEFAULT_SIZE;
					}
				}
				if (nTop < 0) {
					regionRect.top = 0;
					regionRect.bottom = DEFAULT_SIZE;
				} else {
					int nBottom = nTop + DEFAULT_SIZE;
					if (nBottom > screenSize.y) {
						regionRect.bottom = screenSize.y;
						regionRect.top = regionRect.bottom - DEFAULT_SIZE;
					} else {
						regionRect.top = nTop;
						regionRect.bottom = nTop + DEFAULT_SIZE;
					}
				}
				
			}
			listener.onRegionSelected(regionRect, me);
			pressed = false;
			selected = true;
			closeEnabled = true;
			invalidate();
		}
		
	}
	
	private void touchMoving(MotionEvent me) {
		Point screenSize = CameraManager.getScreenSize();
		int x = (int)me.getX();
		int y = (int)me.getY();
		if (pressed && pressedPoint != null) {
			selected = true;
			if (x > widthLimited) {
				x = widthLimited; 
			}
			if (y > screenSize.y) {
				y = screenSize.y;
			}
		// check x
			if (x < pressedPoint.x) {
				regionRect.left = x;
				regionRect.right = pressedPoint.x;
			} else {
				regionRect.right = x;
			}
			
			// check y
			if (y < pressedPoint.y) {
				regionRect.top = y;
				regionRect.bottom =  pressedPoint.y;
			} else {
				regionRect.bottom = y;
			}
		}
		invalidate();
	}
	
}
