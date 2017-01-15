package com.uit.instancesearch.camera.listener;

public interface ActionListener {
	public void onCameraFlashChange(boolean on);
	public void onCameraCapture();
	public void onSelectImage();
	public void onRequestImage(String requestTag, String[] imageIds);
	public void onCompleted(); // on completed a query session
	void onQuerying();
}
