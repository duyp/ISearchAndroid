package com.uit.instancesearch.camera;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationServer;
import com.uit.instancesearch.camera.ProcessingServer.ProcessingServer;
import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.listener.ActionListener;
import com.uit.instancesearch.camera.listener.GoogleServiceListener;
import com.uit.instancesearch.camera.listener.RegionSelectListener;
import com.uit.instancesearch.camera.listener.UITWebServiceListener;
import com.uit.instancesearch.camera.manager.CameraManager;
import com.uit.instancesearch.camera.manager.UITResultViewManager;
import com.uit.instancesearch.camera.manager.WSManager;
import com.uit.instancesearch.camera.tools.ImageTools;
import com.uit.instancesearch.camera.tools.ViewTools;

import android.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CameraActivity extends ActionBarActivity implements RegionSelectListener,
        UITWebServiceListener,
        ActionListener,
        ServersDialogFragment.ServersDialogListener,
        GoogleServiceListener {

	private static final int SELECT_PHOTO = 100;
	
	CameraManager 		cameraManager; 	// camera manager
	WSManager 			wsManager;		// web services manager
	Camera 				camera;			// camera
	
	UITResultViewManager resultViewManager;
	
	CameraPreview 		cPreview;		// camera preview
	RegionSelectionView regionView;		// region selection view
	MenuView			menuView;
	
	ImageView			selectImageView;
	LinearLayout		selectView;
	
	boolean 			imageSelecting;

    ProcessingServer server;
    int selectedServer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove title bar
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

	    //Remove notification bar
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

	   //set content view AFTER ABOVE sequence (to avoid crash)
	    this.setContentView(R.layout.activity_camera);

		// show server choices dialog
		ServersDialogFragment serverDialog = new ServersDialogFragment();
		serverDialog.setCancelable(false);
		serverDialog.show(getFragmentManager(),"servers");

	}

    public void onServersDialogPositiveClick(DialogFragment dialog, int chosenServer) {
        Toast.makeText(this,chosenServer==WSManager.SERVER_UIT?R.string.uit_server:R.string.google_server,Toast.LENGTH_SHORT);
        selectedServer = chosenServer;
        if (chosenServer == WSManager.SERVER_UIT) {
            server = new UITImageRetrievalServer(this, this);
        } else {
            server = new GoogleImageAnnotationServer();
        }
        initialize();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!imageSelecting) {
            releaseCamera();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imageSelecting) {
            camera.stopPreview();
        }
        //camera = CameraManager.getCameraInstance();
    }


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}


    private void initialize() {
        wsManager = new WSManager(this,server);
        imageSelecting = false;
        camera = CameraManager.getCameraInstance();
        if (camera != null) {
            //camera preview
            cPreview = new CameraPreview(this, camera);
            FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
            preview.addView(cPreview);
            //region view
            regionView = (RegionSelectionView)this.findViewById(R.id.region_view);
            regionView.init(this);

            // initialize Camera manager
            cameraManager = new CameraManager(this, camera, cPreview, regionView, wsManager);
            cameraManager.initialize(server,this,this,this);
            //menu view
            menuView = (MenuView)this.findViewById(R.id.menu_view);
            // selected view
            selectView = (LinearLayout)this.findViewById(R.id.selected_view);
            selectImageView = (ImageView)this.findViewById(R.id.selected_image_view);

            // result event manager
            resultViewManager = new UITResultViewManager(this);
        }

        MenuView menuView = (MenuView)this.findViewById(R.id.menu_view);
        menuView.initializeCameraListener(this);

        regionView.setWidthLimited(CameraManager.getScreenSize().x - menuView.getLayoutParams().width);
    }
	
	void hideMenu() {
		menuView.hideMenu();
		menuView.setTouchEnabled(false);
	}
	
	void showMenu() {
		menuView.showMenu();
		menuView.setTouchEnabled(true);
	}
	
	/** A safe way to get an instance of the Camera object. */
    
    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    @Override
    public void onBackPressed() {
    	if (regionView.isScanning()) {
    		regionView.stopScan();
    		wsManager.cancelExecute();
    	}  else if(regionView.isRegionSelected()) {
			regionView.closeRegion();
		} else if (resultViewManager.isShown()) {
    		this.onCompleted();
    	}
    }
    
	@Override
	public void onRegionSelected(Rect regionRect, MotionEvent e) {
		cameraManager.setRegionSelected(regionRect);
	}


	@Override
	public void onRegionConfirmed(Bitmap croppedImage) {
		resultViewManager.setQueryImage(croppedImage);
	}
	
	@Override
	public void onRegionCancelScan() {
		if (imageSelecting) {
			selectView.setVisibility(View.INVISIBLE);
			imageSelecting = false;
		}
		showMenu();
		camera.startPreview();
		cameraManager.resumeFlash();
	}

	@Override
	public void onCompleted() {
		resultViewManager.hideResultView();
		camera.startPreview();
		cameraManager.resumeFlash();
		showMenu();
		regionView.setSelectEnabled(true);
		//resultViewManager.clearResults();
		//regionView.setVisibility(View.VISIBLE);
	}

	// UIT Web Service listener
	@Override
	public void onServerRespond(String[] rankedList) {
		regionView.doneScan();
		
		if (imageSelecting) {
			selectView.setVisibility(View.INVISIBLE);
			imageSelecting = false;
		}
		resultViewManager.clearResults();
		resultViewManager.setResultRankedList(rankedList); // set ranked list 
		resultViewManager.showResultView();
		hideMenu();
		camera.startPreview();
	}
	
	@Override
	public void onRequestImage(String requestTag, String[] imageIds) {
		//resultViewManager.setItemClickEnabled(false);
		//resultViewManager.getQueryTextView().setText(R.string.loading);
		wsManager.executeUITImageRequest(this, requestTag, imageIds);
	}

	@Override
	public void onQuerying() {
		regionView.startScan();
		regionView.setSelectEnabled(false);
		hideMenu();
		Toast.makeText(this, "Analyzing... Press back to cancel.", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onImageRecieved(String tag, String imgId, Bitmap result) {
		if (tag.equals(UITImageRetrievalServer.TAG_QUERY) || tag.equals(UITImageRetrievalServer.TAG_GET_THUMBNAIL_IMAGE)) {
			resultViewManager.addThumbnailImage(imgId, result); // add thumbnail with image id
			//resultViewManager.getQueryTextView().setText(R.string.none);
			//resultViewManager.setItemClickEnabled(true);
		} else if (tag.equals(UITImageRetrievalServer.TAG_GET_PREVIEW_IMAGE)) {
			resultViewManager.setPreviewImage(imgId, result);
			//resultViewManager.getQueryTextView().setText(R.string.none);
			//resultViewManager.setItemClickEnabled(true);
		} else if(tag.equals(UITImageRetrievalServer.TAG_GET_FULL_IMAGE)) {
			// save image
			ImageTools.saveImage(result);
		    Toast.makeText(this, "Image " + imgId + ": saved to downloads directory !", Toast.LENGTH_SHORT).show();
		    resultViewManager.updateDownloadedState(imgId);
		}
	}
	
	@Override
	public void onConnectionError() {
		if (regionView.isScanning()) {
			regionView.stopScan();
		}
		if (resultViewManager.isShown()) {
			//resultViewManager.getQueryTextView().setText(R.string.none);
			//resultViewManager.setItemClickEnabled(true);
		}
	}
	// end Web Service listener
	
	@Override
	public void onCameraFlashChange(boolean on) {
		cameraManager.flashChange(on);
	}

	@Override
	public void onCameraCapture() {
		cameraManager.capture();
		cameraManager.pauseFlash();
	}

	@Override
	public void onSelectImage() {
		selectAnImage();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent){
    	super.onActivityResult(resultCode, resultCode, imageReturnedIntent);
    	switch(requestCode){
    	case SELECT_PHOTO:
    		if (resultCode == RESULT_OK){
    			Uri selectedImage = imageReturnedIntent.getData();
    			//String filename = selectedImage.getLastPathSegment();
    			try {
					InputStream imageStream = this.getContentResolver().openInputStream(selectedImage);
					Bitmap img = BitmapFactory.decodeStream(imageStream);
					img = CameraManager.scaleBitmap(img);
					
					selectView.setVisibility(View.VISIBLE);
					selectImageView.setImageBitmap(img);			
					
					regionView.setRegion(ViewTools.getImageRectInView(selectImageView));
					
					wsManager.executeUITQueryRequest(this,img);
					this.onQuerying();
					this.onRegionConfirmed(img);
					//selectedImageView.setVisibility(View.VISIBLE);
    			} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
    		}
    		break;
    		
    	default: return;
    	}
    }

    public void selectAnImage(){
    	imageSelecting = true;
    	Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    	photoPickerIntent.setType("image/*");
    	this.startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }


}
