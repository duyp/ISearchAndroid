package com.uit.instancesearch.camera.listener;

import android.graphics.Bitmap;

public interface UITWebServiceListener extends ServiceListener {
	void onServerRespond(String[] rankedList);
	void onImageReceived(String tag, String imgId, Bitmap result);
}