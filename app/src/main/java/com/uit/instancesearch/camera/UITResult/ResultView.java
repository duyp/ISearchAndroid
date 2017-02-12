package com.uit.instancesearch.camera.UITResult;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.manager.MyCameraManager;
import com.uit.instancesearch.camera.tools.ButtonRect;

public class ResultView extends LinearLayout{
	
	static final String TITLE = "Result";
	static final String BACK_TEXT = "Close";
	static final int TITLE_SIZE = 50;
	static final int TITLE_PADDING = 0;
	static final int BAR_SIZE = 60;
	static final int GRIDVIEW_PADING = 30;
	static final int LEFT_PADDING = 3;
	public static final int MIN_SIZE = 350;
	static final int GRIDVIEW_MAX_COLUMNS = 3;
	static final int GRIDVIEW_HOR_SPACING = 30;
	static final int GRIDVIEW_VER_SPACING = 30;
	
	final Paint paint;
	final int titleBackColor;
	final int leftColor;
	final int gridViewBackColor;
	final Drawable gradientBar;
	final Drawable gradientLeft;
	
	int gridViewColumns = 3;

	GridView gridView;
	Context context;
	ViewGroup.LayoutParams params;
	
	ButtonRect backButtonRect;
	ImageGridViewAdapter imageAdapter;
	
	Point pressedPoint;
	boolean pressed;
	
	ResultManager manager;
	
	public ResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		titleBackColor = getResources().getColor(R.color.gray10);
		gridViewBackColor = getResources().getColor(R.color.Transparent20);
		
		backButtonRect = new ButtonRect();
		pressedPoint = new Point();
		pressed = false;
		gradientBar = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
        		new int[] {Color.argb (0x60, 125, 125, 125), Color.argb (0, 0, 0, 0) });
		gradientLeft = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
        		new int[] {Color.argb (0x60, 125, 125, 125), Color.argb (0, 0, 0, 0) });
		leftColor = getResources().getColor(R.color.blueColor30);
		init();
	}
	
	void init() {		
		gridView = new GridView(context);
		//gridView.setLeft(1900 / 2);
		//gridView.setBackgroundColor(gridViewBackColor);
		gridView.setNumColumns(GRIDVIEW_MAX_COLUMNS);
		imageAdapter = new ImageGridViewAdapter(context,this); 
		gridView.setAdapter(imageAdapter);
		gridView.setVerticalSpacing(GRIDVIEW_VER_SPACING);
		gridView.setHorizontalSpacing(GRIDVIEW_HOR_SPACING);
		gridView.setVerticalScrollBarEnabled(false);
		//gridView.setVerticalScrollBarEnabled(false);
		
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.topMargin = BAR_SIZE;
		params.leftMargin = GRIDVIEW_PADING * 2 + LEFT_PADDING;
		params.rightMargin = GRIDVIEW_PADING;
		gridView.setPadding(0, GRIDVIEW_PADING, 0, 0);
		this.addView(gridView, 0, params);
	}
	
	public void initManager(ResultManager m) {
		manager = m;
		imageAdapter.initManager(manager);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		
		paint.setColor(titleBackColor);
		canvas.drawRect(LEFT_PADDING, 0, canvas.getWidth(), BAR_SIZE, paint);
		paint.setColor(gridViewBackColor);
		canvas.drawRect(LEFT_PADDING, BAR_SIZE, canvas.getWidth(), canvas.getHeight(), paint);
		
		// left
		paint.setColor(titleBackColor);
		canvas.drawRect(0, 0, LEFT_PADDING, canvas.getHeight() + GRIDVIEW_PADING, paint);
//		
		paint.setTextSize(TITLE_SIZE);
		paint.setColor(Color.WHITE);
		canvas.drawText(TITLE, TITLE_PADDING + LEFT_PADDING + 10, TITLE_SIZE + TITLE_PADDING, paint);
		
		//gradientLeft.setBounds(0, 0, LEFT_PADDING, canvas.getHeight());
		//gradientLeft.draw(canvas);
		
//		backButtonRect.set(canvas.getWidth() - TITLE_SIZE * 3, 0, canvas.getWidth(), BAR_SIZE);
//		canvas.drawText(BACK_TEXT, backButtonRect.left, backButtonRect.top + TITLE_SIZE + TITLE_PADDING, paint);
		
		//gradientBar.setBounds(LEFT_PADDING, BAR_SIZE, canvas.getWidth(), BAR_SIZE + 10);
		//gradientBar.draw(canvas);
		
		super.onDraw(canvas);
	}
	
	public GridView getGridView() {
		return gridView;
	}
	
	@SuppressLint("NewApi")
	public void show() {
		this.animate().translationX(0).setDuration(1000).setInterpolator(new OvershootInterpolator()).start();
	}
	
	@SuppressLint("NewApi")
	public void hide() {
		this.animate().translationX(this.getWidth()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).start();
	}
	
	public void invalidateResult() {
		imageAdapter.notifyDataSetChanged();
		gridView.invalidateViews();
	}
	
	public void restart() {
		gridViewColumns = GRIDVIEW_MAX_COLUMNS;
		gridView.setNumColumns(gridViewColumns);
	}
	
	public int getRowHeight() {
		//return (gridView.getWidth() - gridViewColumns*GRIDVIEW_HOR_SPACING) / gridViewColumns;
		return getColumnWidth();
	}
	
	public int getColumnWidth() {
		return (gridView.getWidth() - gridViewColumns*GRIDVIEW_HOR_SPACING) / gridViewColumns;
	}
	
	int pressedWidth;
	
	public void prepareResize() {
		pressedWidth = this.getWidth();
	}
	
	public int resize(int dx) {
		params = this.getLayoutParams();
		int x = MyCameraManager.getScreenSize().x;
		if (pressedWidth + dx > x / 5 && pressedWidth + dx < x - x/5) {
			params.width = pressedWidth + dx;
			if (params.width <= x/3 && params.width > x/4) {
				gridViewColumns = GRIDVIEW_MAX_COLUMNS - 1;
			} else if (params.width <= x/4){
				gridViewColumns = GRIDVIEW_MAX_COLUMNS - 2;
			} else {
				gridViewColumns = GRIDVIEW_MAX_COLUMNS;
			}
			gridView.setNumColumns(gridViewColumns);
			this.setLayoutParams(params);
			gridView.invalidateViews();
			invalidate();
			return x - params.width;
		}
		return 0;
	}
}
