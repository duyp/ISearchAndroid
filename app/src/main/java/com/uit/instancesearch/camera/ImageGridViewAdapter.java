package com.uit.instancesearch.camera;

import com.uit.instancesearch.camera.manager.ResultManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageGridViewAdapter extends BaseAdapter{

	Context mContext;
	ResultView mParent;
	ResultManager manager;
	
	public final Bitmap getMoreImg;
	public final Bitmap loadingImg;
	
	public ImageGridViewAdapter(Context c, ResultView parent) {
		mContext = c;
		this.mParent = parent;
		getMoreImg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_plus);
		loadingImg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_loading);
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ImageView imageView;
		if(view == null) {
			imageView = new ImageView(mContext);
		} else {
			imageView = (ImageView)view;
		}
		if (manager != null) {
			if (position == getCount() - 1) { // put + icon to last image
				if (manager.isFullThumbnail()) {
					imageView.setImageBitmap(manager.getThumbnails(position));
				} else if (manager.getLoadingThumbCount() == 0) { // if no image are loading
					imageView.setImageBitmap(getMoreImg);
				} else {
					imageView.setImageBitmap(loadingImg);
				}
			} else if (position < manager.getThumbnailCount()){
				imageView.setImageBitmap(manager.getThumbnails(position));
			} else {
				imageView.setImageBitmap(loadingImg);
			}
			AbsListView.LayoutParams p = new AbsListView.LayoutParams(mParent.getColumnWidth(), mParent.getRowHeight());
			imageView.setLayoutParams(p);
		}
		return imageView;
	}

	public void initManager(ResultManager m) {
		manager = m;
	}
	
	@Override
	public int getCount() {
		if (manager != null) {
			int n = 1;
			if (manager.getLoadingThumbCount() > 0 || manager.isFullThumbnail())
				n = 0;
			return manager.getThumbnailCount() + n + manager.getLoadingThumbCount();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (manager != null)
			if (position < manager.getThumbnailCount())
				return manager.getThumbnails(position);
			else if (position == manager.getThumbnailCount())
				return getMoreImg;
			else 
				return loadingImg;
		return null;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
}
