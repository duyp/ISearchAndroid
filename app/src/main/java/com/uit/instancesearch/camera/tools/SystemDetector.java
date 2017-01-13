package com.uit.instancesearch.camera.tools;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;

public class SystemDetector {
	
	public static boolean checkCameraHardware(Context context) {
		return (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
	}
	
	public static boolean checkCameraAutoFocus(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
	}
	
	public static boolean checkCameraFlash(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}
}
