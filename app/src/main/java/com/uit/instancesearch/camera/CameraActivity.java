package com.uit.instancesearch.camera;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationServer;
import com.uit.instancesearch.camera.ProcessingServer.ProcessingServer;
import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.listener.ActionListener;
import com.uit.instancesearch.camera.listener.GoogleCloudVisionListener;
import com.uit.instancesearch.camera.listener.RegionSelectListener;
import com.uit.instancesearch.camera.listener.UITWebServiceListener;
import com.uit.instancesearch.camera.manager.CameraManager;
import com.uit.instancesearch.camera.manager.GoogleAccountManager;
import com.uit.instancesearch.camera.manager.UITResultViewManager;
import com.uit.instancesearch.camera.manager.WSManager;
import com.uit.instancesearch.camera.tools.ImageTools;
import com.uit.instancesearch.camera.tools.ViewTools;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.os.Process;
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
        GoogleCloudVisionListener {

    private static final int REQUEST_SELECT_PHOTO = 100;
    public static final int REQUEST_ACCOUNT_AUTHORIZATION = 42;
    private static final int REQUEST_PICK_ACCOUNT = 41;

    CameraManager cameraManager;    // camera manager
    WSManager wsManager;        // web services manager
    Camera camera;            // camera

    UITResultViewManager resultViewManager;

    CameraPreview cPreview;        // camera preview
    RegionSelectionView regionView;        // region selection view
    MenuView menuView;

    ImageView selectImageView;
    LinearLayout selectView;

    boolean imageSelecting = false;
    boolean accessTokenGetting = false;

    ProcessingServer server;
    int selectedServer;

    GoogleAccountManager googleAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(R.layout.activity_camera);

        // show server selection dialog
        ServersDialogFragment serverDialog = new ServersDialogFragment();
        serverDialog.setCancelable(false);
        serverDialog.show(getFragmentManager(), "servers");
    }

    public void onServersDialogPositiveClick(DialogFragment dialog, int chosenServer) {
        selectedServer = chosenServer;
        if (chosenServer == WSManager.SERVER_UIT) {
            server = new UITImageRetrievalServer(this, this);
            initialize();
        } else {
            pickAccount();
        }
    }

    private void pickAccount() {
        accessTokenGetting = true;
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        Intent i = AccountPicker.newChooseAccountIntent(null, null, accountTypes,
                false, null, null, null, null);
        startActivityForResult(i, REQUEST_PICK_ACCOUNT);
    }

    private void connectGoogleAccount(Account a) {
        googleAccountManager = new GoogleAccountManager(a, this);
        googleAccountManager.getAuthToken();
    }

    public void onTokenReceived() {
        String accessToken = googleAccountManager.getAccessToken();
        if(accessToken != null) {
            Toast.makeText(this,accessToken,Toast.LENGTH_SHORT);
            server = new GoogleImageAnnotationServer(this,accessToken);
            initialize();
            accessTokenGetting = false;
        }
    }

    @Override
    protected void onStop() {
        super.onPause();
        if (!imageSelecting && !accessTokenGetting) {
            releaseCamera();
            Process.killProcess(Process.myPid());
        }
    }

    @Override
    protected void onRestart() {
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
        wsManager = new WSManager(this, server);
        imageSelecting = false;
        camera = CameraManager.getCameraInstance();
        if (camera != null) {
            //camera preview
            cPreview = new CameraPreview(this, camera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cPreview);
            //region view
            regionView = (RegionSelectionView) this.findViewById(R.id.region_view);
            regionView.init(this);

            // initialize Camera manager
            cameraManager = new CameraManager(this, camera, cPreview, regionView, wsManager);
            cameraManager.initialize(server, this, this);
            //menu view
            menuView = (MenuView) this.findViewById(R.id.menu_view);
            // selected view
            selectView = (LinearLayout) this.findViewById(R.id.selected_view);
            selectImageView = (ImageView) this.findViewById(R.id.selected_image_view);

            // result event manager
            resultViewManager = new UITResultViewManager(this);
        }

        MenuView menuView = (MenuView) this.findViewById(R.id.menu_view);
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

    /**
     * A safe way to get an instance of the Camera object.
     */

    private void releaseCamera() {
        if (camera != null) {
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (regionView.isScanning()) {
            regionView.stopScan();
            wsManager.cancelExecute();
        } else if (regionView.isRegionSelected()) {
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

    // GOOGLE listener
    @Override
    public void onCloudVisionResponse(BatchAnnotateImagesResponse response) {
        Toast.makeText(this,"GOOGLE RESPOND",Toast.LENGTH_LONG);
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
        wsManager.executeUITImageRequest(requestTag, imageIds);
    }

    @Override
    public void onQuerying() {
        regionView.startScan();
        regionView.setSelectEnabled(false);
        hideMenu();
        Toast.makeText(this, "Analyzing... Press back to cancel.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageReceived(String tag, String imgId, Bitmap result) {
        if (tag.equals(UITImageRetrievalServer.TAG_QUERY) ||
                tag.equals(UITImageRetrievalServer.TAG_GET_THUMBNAIL_IMAGE)) {
            resultViewManager.addThumbnailImage(imgId, result); // add thumbnail with image id
            //resultViewManager.getQueryTextView().setText(R.string.none);
            //resultViewManager.setItemClickEnabled(true);
        } else if (tag.equals(UITImageRetrievalServer.TAG_GET_PREVIEW_IMAGE)) {
            resultViewManager.setPreviewImage(imgId, result);
            //resultViewManager.getQueryTextView().setText(R.string.none);
            //resultViewManager.setItemClickEnabled(true);
        } else if (tag.equals(UITImageRetrievalServer.TAG_GET_FULL_IMAGE)) {
            // save image
            ImageTools.saveImage(result);
            Toast.makeText(this, "Image " + imgId + ": saved to downloads directory !",
                    Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(resultCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = intent.getData();
                    //String filename = selectedImage.getLastPathSegment();
                    try {
                        InputStream imageStream = this.getContentResolver().openInputStream(selectedImage);
                        Bitmap img = BitmapFactory.decodeStream(imageStream);
                        img = CameraManager.scaleBitmap(img);

                        selectView.setVisibility(View.VISIBLE);
                        selectImageView.setImageBitmap(img);

                        regionView.setRegion(ViewTools.getImageRectInView(selectImageView));

                        wsManager.executeUITQueryRequest(img);
                        this.onQuerying();
                        this.onRegionConfirmed(img);
                        //camera.release();
                        //selectedImageView.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    String email = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    AccountManager am = AccountManager.get(this);
                    // MUST check permission for android 6
                    Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                    for (Account a : accounts) {
                        if (a.name.equals(email))  {
                            connectGoogleAccount(a);
                            return;
                        }
                    }
                }
                Toast.makeText(this, "PLEASE SELECT AN ACCOUNT AGAIN !", Toast.LENGTH_SHORT).show();
                pickAccount();
            default:
                return;
        }
    }

    public void selectAnImage() {
        imageSelecting = true;
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK).setType("image/*");
        this.startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
    }
}
