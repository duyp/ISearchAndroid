package com.uit.instancesearch.camera.tools;

import android.graphics.Rect;
import android.widget.ImageView;

public class ViewTools {
	
	public static Rect getImageRectInView(ImageView imageView) {
		if (imageView.getDrawable() != null) {
			int h = imageView.getMeasuredHeight(); //height of imageView
			int w = imageView.getMeasuredWidth(); //width of imageView
			int ih=h;
			int iw=w;
			int iH=imageView.getDrawable().getIntrinsicHeight();//original height of underlying image
			int iW=imageView.getDrawable().getIntrinsicWidth();//original width of underlying image
			
			if (ih/iH<=iw/iW) iw=iW*ih/iH;//rescaled width of image within ImageView
			else ih= iH*iw/iW;//rescaled height of image within ImageView
			
			float left, top, right, bottom;
			if (iw == w) {
				left = 0;
				right = iw;
				top = (h-ih)/2;
				bottom = top+ih;
			} else {
				top = 0;
				bottom = ih;
				left = (w-iw)/2;
				right = left+iw;
			}
			return new Rect((int)left,(int)top,(int)right,(int)bottom);
			//return new Rect(0,0,iw,ih);
		}
		return null;
	}
}
