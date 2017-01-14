package com.uit.instancesearch.camera.tools;

import java.io.ByteArrayOutputStream;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

public class ImageTools {

	public static String encodeBitmap(Bitmap bm){
		if (bm == null) return null;
		
		long t = System.currentTimeMillis();
		ByteArrayOutputStream obj = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 30, obj);
		byte[] byteArray = obj .toByteArray();
		String result = Base64.encodeToString(byteArray, Base64.DEFAULT);
		Log.d("debug","Encode Time: " + (System.currentTimeMillis() - t) + "ms");
		Log.d("debug","Data size: " + result.length() / 1024 +"kB");
		return result;
	}
	
}
