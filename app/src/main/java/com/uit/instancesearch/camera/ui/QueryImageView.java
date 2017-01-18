package com.uit.instancesearch.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class QueryImageView extends ImageView {
	
	static final int GRADIENT_SIZE = 10;
	
	final GradientDrawable btGradient;
	final GradientDrawable rlGradient;
	final GradientDrawable lrGradient;
	final GradientDrawable tbGradient;
	
	public QueryImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		btGradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
        		new int[] {Color.argb (0x60, 255, 255, 255), Color.argb (0, 255, 255, 255) });
		rlGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
        		new int[] {Color.argb (0x60, 255, 255, 255), Color.argb (0, 255, 255, 255) });
		lrGradient = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
        		new int[] {Color.argb (0x60, 255, 255, 255), Color.argb (0, 255, 255, 255) });
		tbGradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
        		new int[] {Color.argb (0x60, 255, 255, 255), Color.argb (0, 255, 255, 255) });
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (this.getDrawable() != null) {
			int ih=this.getMeasuredHeight();//height of imageView
			int iw=this.getMeasuredWidth();//width of imageView
			int iH=this.getDrawable().getIntrinsicHeight();//original height of underlying image
			int iW=this.getDrawable().getIntrinsicWidth();//original width of underlying image
			if (ih/iH<=iw/iW) iw=iW*ih/iH;//rescaled width of image within ImageView
			else ih= iH*iw/iW;//rescaled height of image within ImageView
			
			
			int left, top, right, bottom;
			if (iw>ih) {
				left = 0;
				right = iw;
				top = (canvas.getHeight()-ih)/2;
				bottom = top+ih;
				
				
			} else {
				top = 0;
				bottom = ih;
				left = (canvas.getWidth()-iw)/2;
				right = left+iw;
				
				
			}
			
			rlGradient.setBounds(left, top, left + GRADIENT_SIZE, bottom);
			rlGradient.draw(canvas);
			
			lrGradient.setBounds(right - GRADIENT_SIZE, top, right, bottom);
			lrGradient.draw(canvas);
			
			btGradient.setBounds(left, top, right, top + GRADIENT_SIZE);
			btGradient.draw(canvas);
			
			tbGradient.setBounds(left, bottom - GRADIENT_SIZE, right, bottom);
			tbGradient.draw(canvas);
		}
	}
}
