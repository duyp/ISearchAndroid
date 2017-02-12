package com.uit.instancesearch.camera.UITResult;

import java.util.Vector;

import android.graphics.Bitmap;
import android.util.Log;

public class ResultManager {
	
	Vector<String> rankedList;
	Vector<Bitmap> thumbnails;
	Vector<Bitmap> previews;
	Vector<String> downloaded;
	
	int thumbCount;
	int loadingThumbCount;
	
	public ResultManager() {
		reset();
		downloaded = new Vector<String>();
	}
	
	public void reset() {
		thumbCount = 0;
		loadingThumbCount = 0;
		rankedList = new Vector<String>();
		thumbnails = new Vector<Bitmap>();
		previews  = new Vector<Bitmap>();
	}
	
	public void setRankedList(String[] list) {
		for(String s : list) {
			//Log.d("debug", s);
			rankedList.add(s);
			thumbnails.add(null);
			previews.add(null);
		}
	}
	
	public String getRankAt(int position) {
		return rankedList.elementAt(position);
	}
	
	public int getPosition(String imgId) {
		return rankedList.indexOf(imgId);
	}
	
	public void setThumbnail(String imgId, Bitmap bm) {
		int i = rankedList.indexOf(imgId);
		if (i != -1 && thumbnails.elementAt(i) == null) {
			thumbnails.set(i, bm);
			thumbCount ++;
			if (loadingThumbCount > 0)
				loadingThumbCount --;
		}
	}
	
	public void setPreview(String imgId, Bitmap bm) {
		int i = rankedList.indexOf(imgId);
		if (i != -1)
			previews.set(i, bm);
	}
	
	public String getImageId(int position) {
		return rankedList.elementAt(position);
	}
	
	public Bitmap getThumbnails(int position) {
		if (position >= thumbnails.size() || position < 0) {
			return null;
		}
		return thumbnails.elementAt(position);
	}
	
	public Bitmap getThumbnails(String imgId) {
		int i = rankedList.indexOf(imgId);
		return getThumbnails(i);
	}
	
	public Bitmap getPreview(int position) {
		if (position >= previews.size() || position < 0) {
			return null;
		}
		return previews.elementAt(position);
	}
	
	public Bitmap getImage(int position) {
		Bitmap bm = getPreview(position);
		if (bm == null) {
			bm = getThumbnails(position);
		}
		return bm;
	}
	
	public Bitmap getPreview(String imgId) {
		int i = rankedList.indexOf(imgId);
		return getPreview(i);
	}
	
	public int getThumbnailCount() {
		return thumbCount;
	}
	
	public int getLoadingThumbCount() {
		return loadingThumbCount;
	}
	
	public boolean isFullThumbnail() {
		return getThumbnailCount() == rankedList.size();
	}
	
	public void setLoadingThumbCount(int n) {
		loadingThumbCount = n;
	}
	
	public int getRankedListCount() {
		return rankedList.size();
	}
	
	public void clearResult() {
		rankedList.clear();
		thumbnails.clear();
		previews.clear();
		reset();
	}
	
	public void setDownloaded(String imgId) {
		if (!downloaded.contains(imgId)) {
			downloaded.add(imgId);
		}
	}
	
	public boolean isDownloaded(int position) {
		return isDownloaded(rankedList.elementAt(position));
	}
	
	public boolean isDownloaded(String imgId) {
		return downloaded.contains(imgId);
	}
	
}
