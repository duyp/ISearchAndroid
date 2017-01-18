package com.uit.instancesearch.camera.ui;

import com.uit.instancesearch.camera.manager.ResultManager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class ImagePagerAdapter extends PagerAdapter {

	static final int ITEM_LEFT = 0;
	static final int ITEM_CENTER = 1;
	static final int ITEM_RIGHT = 2;
	
	Context context;
	ResultManager manager;

	public ImagePagerAdapter(Context c, ResultManager m) {
		this.context = c;
		this.manager = m;
	}
	
	@Override
	public int getCount() {
		return manager.getThumbnailCount();
	}
	
	@Override
	public int getItemPosition(Object obj) {
		return POSITION_NONE;
	}

	@Override
	public boolean isViewFromObject(View v, Object obj) {
		return v == obj;
	}
	
	 @Override
	 public Object instantiateItem(View container, int position) {
		 //Log.d("debug", "Pager pos: " + position);
		 ImageView imageView = new ImageView(context);
		 int padding = 10;
		 imageView.setPadding(padding, padding, padding, padding);
		 imageView.setImageBitmap(manager.getImage(position));
		 ((ViewPager) container).addView(imageView);
		 return imageView;
	 }
	 
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((ImageView) object);
	}
	
}
