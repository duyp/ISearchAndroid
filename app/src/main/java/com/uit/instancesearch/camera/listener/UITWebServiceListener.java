package com.uit.instancesearch.camera.listener;

import android.graphics.Bitmap;

public interface UITWebServiceListener {
	public void onQuerying();
	public void onServerRespond(String[] rankedlist);
	public void onImageRecieved(String tag, String imgId, Bitmap result);
	public void onConnectionError();
}