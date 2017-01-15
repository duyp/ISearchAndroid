package com.uit.instancesearch.camera.listener;

import android.graphics.Bitmap;

public interface UITWebServiceListener extends ServiceListener{
	public void onServerRespond(String[] rankedlist);
	public void onImageReceived(String tag, String imgId, Bitmap result);
	public void onConnectionError();
}