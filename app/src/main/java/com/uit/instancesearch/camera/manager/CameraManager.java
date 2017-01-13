package com.uit.instancesearch.camera.manager;

import java.util.List;

import com.uit.instancesearch.camera.CameraPreview;
import com.uit.instancesearch.camera.RegionSelectionView;
import com.uit.instancesearch.camera.listener.RegionSelectListener;
import com.uit.instancesearch.camera.listener.WebServiceListener;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraManager {
	
	static final int MAX_WIDTH = 500;
	static final int MAX_HEIGHT = 500;
	
	Camera mCamera;
	Camera.Parameters params;
	CameraPreview mPreview;
	RegionSelectionView regionView;
	
	WSManager wsManager;
	
	boolean flashOn;
	
	static Context context;
	
	WebServiceListener wsListener;
	RegionSelectListener rsListener;
	
	// capture
	private PictureCallback mPicture = new PictureCallback() {
			
			@Override
			public void onPictureTaken(byte[] data, Camera c) {
				long t = System.currentTimeMillis();
				Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				Rect r = regionView.getRegion();
				if (r != null) {
					bm = cropBitmap(bm, r);
				}
				bm = scaleBitmap(bm);
				System.out.println("Extract data Time: " + (System.currentTimeMillis() - t) + "ms");
				wsManager.executeQueryRequest(bm);
				wsListener.onQuerying();
				rsListener.onRegionConfirmed(bm);
				
			}
		};
		
	public CameraManager(Context c, Camera camera, CameraPreview preview, RegionSelectionView rs, WSManager wsm) {
		mCamera = camera;
		mPreview = preview;
		regionView = rs;
		wsManager = wsm;
		context = c;
		//initialize();
	}
	
	public void initialize(WebServiceListener l, RegionSelectListener l1) {
		wsListener = l;
		rsListener = l1;
		setCameraFocus(mCamera);
		flashOn = false;
		params = mCamera.getParameters();
        params.setJpegQuality(50);
        params.setColorEffect(Camera.Parameters.EFFECT_NONE);
        params.setPictureFormat(ImageFormat.JPEG);
        
        //setting picture size
        List<Size> sizes = params.getSupportedPreviewSizes();
        Size optimalSize = CameraPreview.getOptimalPreviewSize(sizes, context.getResources().getDisplayMetrics().widthPixels, context.getResources().getDisplayMetrics().heightPixels);
        //params.setPictureSize(optimalSize.width, optimalSize.height);
        params.setPictureSize(optimalSize.width, optimalSize.height);
        //Toast t = Toast.makeText(context, "Image Size: " + optimalSize.width + "x" + optimalSize.height, Toast.LENGTH_LONG);
        //t.show();
        mCamera.setParameters(params);
	}	
	

	public PictureCallback getPictureCallback() {
		return mPicture;
	}
	
	public static Bitmap cropBitmap(Bitmap bm, Rect r) {
		Point screenSize = getScreenSize();
		float wRatio = (float)bm.getWidth() / screenSize.x;
		float hRatio = (float)bm.getHeight()/ screenSize.y;
		int x = (int)(r.left*wRatio);
		int y = (int)(r.top*hRatio);
		int width =  (int)((r.right-r.left)*wRatio);
		int height = (int)((r.bottom-r.top)*hRatio);
		return Bitmap.createBitmap(bm, x, y, width, height);
	}
	
	public static Bitmap scaleBitmap(Bitmap bm) {
		int w = bm.getWidth();
		int h = bm.getHeight();
		if (w*h > MAX_WIDTH*MAX_HEIGHT) {
			float x = (float)w/h;
			return Bitmap.createScaledBitmap(bm, (int)(w>h?MAX_WIDTH:MAX_HEIGHT*x), (int)(h>w?MAX_HEIGHT:MAX_WIDTH/x), true);
		}
		return bm;
	}

	public void setRegionSelected(Rect r){
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//			params = mCamera.getParameters();
//			ArrayList<Camera.Area> areas = new ArrayList<>();
//			areas.add(new Camera.Area(r, 1000));
//			
//			
//			if(params.getMaxNumFocusAreas()>0){
//				params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//				params.setFocusAreas(areas);
//				params.setMeteringAreas(areas);
//			 }
//			
//			mCamera.setParameters(params);
//		}
	}
	
	@SuppressLint("NewApi")
	public static Point getScreenSize() {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Point screenSize = new Point();
		Display p = wm.getDefaultDisplay();
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			p.getSize(screenSize);
		} else {
			screenSize.set(p.getWidth(), p.getHeight());
		}
		return screenSize;
	}
	
	
    public static void setCameraFocus(Camera mCamera) {
    	Camera.Parameters parameters = mCamera.getParameters();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH &&
            supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // auto focus on request only
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
    }
    
    
    
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

	
	public void flashChange(boolean flashOn) {
		this.flashOn = flashOn;
		params = mCamera.getParameters();
		params.setFlashMode(flashOn ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
		mCamera.setParameters(params);
		//Toast.makeText(context, flashOn + "-", Toast.LENGTH_SHORT).show();
	}
	
	public void pauseFlash() {
		if (flashOn) {
			params = mCamera.getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(params);
		}
	}
	
	public void resumeFlash() {
		if (flashOn) {
			params = mCamera.getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(params);
		}
	}
	
	public void	capture() {
//		if (flashOn) {
//			params = mCamera.getParameters();
//			params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//			mCamera.setParameters(params);
//		}
		mCamera.takePicture(null, null, mPicture);
//		onFlashChange(flashOn);
	}
}
