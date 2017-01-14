package com.uit.instancesearch.camera.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;
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

	public static void saveImage(Bitmap bm) {
		FileOutputStream out;
		try {
			File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			String outFile = f.toString() + "/issearch_" + StringTools.getRandomNumberString(10) + ".jpg";
			out = new FileOutputStream(outFile);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
