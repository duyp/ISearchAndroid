package com.uit.instancesearch.camera.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.api.services.vision.v1.model.Image;

public class ImageTools {

	public static String encodeBitmapToString(Bitmap bm){
		if (bm == null) return null;
		
		long t = System.currentTimeMillis();
		ByteArrayOutputStream obj = new ByteArrayOutputStream();

		// compress
		bm.compress(Bitmap.CompressFormat.JPEG, 30, obj);

		byte[] byteArray = obj .toByteArray();
		String result = Base64.encodeToString(byteArray, Base64.DEFAULT);
		Log.d("debug","Encode Time: " + (System.currentTimeMillis() - t) + "ms");
		Log.d("debug","Data size: " + result.length() / 1024 +"kB");
		return result;
	}

	public static Image getBase64EncodedJpeg(Bitmap bitmap) {
		Image image = new Image();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
		byte[] imageBytes = byteArrayOutputStream.toByteArray();
		image.encodeContent(imageBytes);
		return image;
	}

	public static Bitmap decodeStringToBitmap(String img) {
		byte[] b = Base64.decode(img,Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(b,0,b.length);
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
