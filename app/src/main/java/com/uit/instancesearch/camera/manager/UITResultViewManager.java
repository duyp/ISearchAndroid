package com.uit.instancesearch.camera.manager;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uit.instancesearch.camera.CameraActivity;
import com.uit.instancesearch.camera.ImagePagerAdapter;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.ResultView;
import com.uit.instancesearch.camera.listener.ActionListener;
import com.uit.instancesearch.camera.listener.ResultListener;
import com.uit.instancesearch.camera.listener.ResultTouchListener;
import com.uit.instancesearch.camera.tools.StringTools;

public class UITResultViewManager implements ResultListener {

	ResultView 			resultView;

	FrameLayout			queryView;
	ImageView			queryImageView;
	//TextView			queryTextView;
	GridView			resultGridView;
	ViewPager			imagePager;
	ImagePagerAdapter	previewAdapter;
	
	LinearLayout		resultMenu;
	ImageButton			downloadButton;
	
	CameraActivity		mainActivity;
	
	ResultManager manager;
	ActionListener listener;
	
	boolean viewing;
	//boolean itemClickEnabled;
	
	boolean isPagerShown = false;
	
	public UITResultViewManager(CameraActivity mainActivity) {
		this.mainActivity = mainActivity;
		
		queryImageView = new ImageView(mainActivity);
		queryView = (FrameLayout)mainActivity.findViewById(R.id.image_view);
		
		//queryTextView = (TextView)mainActivity.findViewById(R.id.query_text_view);
		resultView = (ResultView)mainActivity.findViewById(R.id.result_view);
		
		manager = new ResultManager();
		resultView.initManager(manager);
		resultGridView = resultView.getGridView();
		
		imagePager = new ViewPager(mainActivity);
		previewAdapter = new ImagePagerAdapter(mainActivity, manager);
		imagePager.setAdapter(previewAdapter);
		
		resultMenu = (LinearLayout)mainActivity.findViewById(R.id.result_menu);
		downloadButton = (ImageButton)mainActivity.findViewById(R.id.save_button);
		
		init();
	}
	
	private void init() {
		viewing = false;
		//itemClickEnabled = true;
		
		OnTouchListener touchListener = new View.OnTouchListener() {
			int pressedX = 0;
			boolean pressed = false;
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if (e.getAction() == MotionEvent.ACTION_DOWN) {
					pressed = true;
					pressedX = (int)e.getRawX();
					onResultViewPrepareResize();
				} else if (e.getAction() == MotionEvent.ACTION_MOVE){
					if (pressed) {
						int dx = pressedX - (int)e.getRawX();
						//Log.d("debug", "x: " + e.getRawX());
						onResultViewResize(dx);
					}
				} else if (e.getAction() == MotionEvent.ACTION_UP) {
					pressed = false;
					onResultViewPrepareResize();
				}
				return true;
			}
		};
		
		listener = (ActionListener)mainActivity;
		resultView.setOnTouchListener(touchListener);
		//queryView.setOnTouchListener(new ResultTouchListener(this));
		queryImageView.setOnTouchListener(touchListener);
		
		resultGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//if (itemClickEnabled) {
					itemClick(position);
				//}
			}
		});
		
		imagePager.setOffscreenPageLimit(2);
		imagePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				if (isPagerShown)
					setPreview(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		
		downloadButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downloadCurrentImage();
			}
		});
	}

	public void setResultRankedList(String[] rankedList) {
		manager.setRankedList(rankedList);
	}
	
	public void addThumbnailImage(String imgId, Bitmap bm) {
		manager.setThumbnail(imgId, bm);
		resultView.invalidateResult();
		previewAdapter.notifyDataSetChanged();
	}
	
	public void setPreviewImage(String imgId, Bitmap bm) {
		manager.setPreview(imgId, bm);
		if (isPagerShown && manager.getPosition(imgId) == imagePager.getCurrentItem()) {
			previewAdapter.notifyDataSetChanged();
		}
	}
	
	public void downloadCurrentImage() {
		int pos = imagePager.getCurrentItem();
		String imgId = manager.getImageId(pos);
		if (!manager.isDownloaded(imgId))	{
			listener.onRequestImage(WSManager.TAG_GET_FULL_IMAGE, new String[] {imgId});
			Toast.makeText(mainActivity, "Downloading image ...", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mainActivity, "Just downloaded !", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void updateDownloadedState(String imgId) {
		manager.setDownloaded(imgId);
	}

	@Override
	public void onResultViewResize(int dx) {
		//queryTextView.setText(String.valueOf(dx));
		int x;
		if ((x=resultView.resize(dx)) != 0) {
			ViewGroup.LayoutParams p1 = queryView.getLayoutParams();
			p1.width = x;
			queryView.setLayoutParams(p1);
			//queryView.invalidate();
			
			ViewGroup.LayoutParams p2 = resultMenu.getLayoutParams();
			p2.width = p1.width;
			resultMenu.setLayoutParams(p2);
			//resultMenu.invalidate();
		}
	}

	@Override
	public void onResultViewPrepareResize() {
		resultView.prepareResize();
	}

	@Override
	public void onResultViewResized() {
		resultView.prepareResize();
	}
	
	private void itemClick(int position) {
		if (position < manager.getThumbnailCount()) { // click for preview image
			if (!isPagerShown) {
				showImagePager();
				showResultMenu();
			}
			imagePager.setCurrentItem(position, false);
		} else if (position == manager.getThumbnailCount() + manager.getLoadingThumbCount()) { // click for get more image
			int remaining = manager.getRankedListCount() - manager.getThumbnailCount();
			int nImage = remaining;
			if (remaining > WSManager.MAX_IMAGE_PER_REQUEST) {
				nImage = WSManager.MAX_IMAGE_PER_REQUEST;
			}
			manager.setLoadingThumbCount(nImage);
			resultView.invalidateResult();
			
			// request images
			String[] imageIds = new String[nImage];
			for(int i = 0; i < nImage; i++) {
				imageIds[i] = manager.getRankAt(manager.getThumbnailCount() + i);
			}
			listener.onRequestImage(WSManager.TAG_GET_THUMBNAIL_IMAGE, imageIds);
		} else {
			
		}
	}
	
	private void setPreview(int position) {
		if (manager.getPreview(position) == null) {
			requestPreviewImage(position);
		}
	}
	
	private void requestPreviewImage(int position) {
		String[] imageId = new String[] {manager.getImageId(position)};
		listener.onRequestImage(WSManager.TAG_GET_PREVIEW_IMAGE, imageId);
	}
	
	public void showResultMenu() {
		resultMenu.animate().translationY(0).setDuration(1000).
		setInterpolator(new OvershootInterpolator()).start();
	}
	
	public void hideResultMenu() {
		resultMenu.animate().translationY(resultMenu.getHeight()).setDuration(1000).
		setInterpolator(new OvershootInterpolator()).start();
	}
	
	void showImagePager() {
		isPagerShown = true;
		queryView.removeView(queryImageView);
		queryView.addView(imagePager);
	}
	
	void showQueryImageView() {
		isPagerShown = false;
		queryView.removeView(imagePager);
		queryView.addView(queryImageView);
	}

	public void showResultView() {
		// result view
		ViewGroup.LayoutParams p = resultView.getLayoutParams();
		p.width = CameraManager.getScreenSize().x*2/5;
		resultView.setLayoutParams(p);
		resultView.restart();
		resultView.show();
		
		// query view
		ViewGroup.LayoutParams p1 = queryView.getLayoutParams();
		p1.width = CameraManager.getScreenSize().x - p.width;
		queryView.setLayoutParams(p1);
		
		//API 14
		queryView.animate().translationX(0).setDuration(1000).
			setInterpolator(new OvershootInterpolator()).start();
		viewing = true;
		
		ViewGroup.LayoutParams p2 = resultMenu.getLayoutParams();
		p2.width = p1.width;
		resultMenu.setLayoutParams(p2);
		
		showQueryImageView();
	}
	
	public void hideResultView() {
		resultView.hide();
		hideResultMenu();
		queryView.animate().translationX(-queryView.getWidth()).setDuration(500)
			.setInterpolator(new AccelerateDecelerateInterpolator()).start();
		viewing = false;
		//imagePager.setCurrentItem(0);
	}
	
	public void setQueryImage(Bitmap bm) {
		queryImageView.setImageBitmap(bm);
	}
	
//	public TextView getQueryTextView() {
//		return queryTextView;
//	}
	
	public boolean isShown() {
		return viewing;
	}
	
	public void clearResults() {
		queryView.removeAllViews();
		manager.clearResult();
		resultView.invalidateResult();
	}
	
//	public void setItemClickEnabled(boolean enabled) {
//		//itemClickEnabled = enabled;
//		resultGridView.setClickable(enabled);
//	}

}
