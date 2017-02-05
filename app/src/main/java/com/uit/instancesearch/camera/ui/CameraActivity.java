package com.uit.instancesearch.camera.ui;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.GoogleVisionResultData;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationServer;
import com.uit.instancesearch.camera.ProcessingServer.ProcessingServer;
import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.ui.GoogleVisionResult.GoogleVisionResultActivity;
import com.uit.instancesearch.camera.ui.dialog.ErrorDialog;
import com.uit.instancesearch.camera.ui.dialog.MyCircleProgressBar;
import com.uit.instancesearch.camera.ui.dialog.ServersDialogFragment;
import com.uit.instancesearch.camera.listener.ActionListener;
import com.uit.instancesearch.camera.listener.GoogleCloudVisionListener;
import com.uit.instancesearch.camera.listener.RegionSelectListener;
import com.uit.instancesearch.camera.listener.UITWebServiceListener;
import com.uit.instancesearch.camera.manager.MyCameraManager;
import com.uit.instancesearch.camera.manager.GoogleAccountManager;
import com.uit.instancesearch.camera.manager.UITResultViewManager;
import com.uit.instancesearch.camera.manager.WSManager;
import com.uit.instancesearch.camera.tools.ImageTools;
import com.uit.instancesearch.camera.tools.ViewTools;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONObject;

public class CameraActivity extends AppCompatActivity implements RegionSelectListener,
        UITWebServiceListener,
        ActionListener,
        ServersDialogFragment.ServersDialogListener,
        GoogleCloudVisionListener {

    private static final int REQUEST_SELECT_PHOTO = 100;
    public static final int REQUEST_ACCOUNT_AUTHORIZATION = 42;
    private static final int REQUEST_PICK_ACCOUNT = 41;
    private static final int REQUEST_ACCOUNT_PERMISSION = 43;
    private static final int REQUEST_CAMERA_PERMISSION = 44;
    private static final int REQUEST_SHOW_RESULT = 46;


    MyCameraManager cameraManager;    // camera manager
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
    boolean isShowResult = false;

    ProcessingServer server;
    int selectedServer;

    GoogleAccountManager googleAccountManager;

    // temp
    Bitmap queryImage;

    long lastBackPressed = -1;
    public static long PRESS_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)

        // show server selection dialog
        showServerSelectionDialog();
    }

    void showServerSelectionDialog() {
        ServersDialogFragment serverDialog = new ServersDialogFragment();
        serverDialog.setCancelable(false);
        serverDialog.show(getFragmentManager(), "servers");
    }

    public void onServersDialogPositiveClick(DialogFragment dialog, int chosenServer) {
        selectedServer = chosenServer;
        if (chosenServer == WSManager.SERVER_UIT) {
            server = new UITImageRetrievalServer(this, this);
            checkCameraPermission();
        } else {
            // check and request Account permission
            checkAccountPermission();
        }
    }

    void checkAccountPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[] {Manifest.permission.GET_ACCOUNTS},
                    REQUEST_ACCOUNT_PERMISSION);
        } else {
            pickAccount();
        }
    }

    void checkCameraPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[] {Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickAccount();
                } else {
                    Toast.makeText(CameraActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    showServerSelectionDialog();
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                initialize();
                break;
            default: break;
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
        MyCircleProgressBar.show(this,"Requesting Google access token...");
    }

    public void onTokenReceived() {
        accessTokenGetting = false;
        MyCircleProgressBar.dismiss();
        String accessToken = googleAccountManager.getAccessToken();
        if(accessToken != null) {
            Toast.makeText(this,accessToken,Toast.LENGTH_SHORT);
            server = new GoogleImageAnnotationServer(this,accessToken);
            checkCameraPermission();
         } else {
            Toast.makeText(this,"Cannot access to Google server!",Toast.LENGTH_SHORT).show();
            showServerSelectionDialog();
        }
    }

    // initialize necessary parameter
    private void initialize() {
        this.setContentView(R.layout.activity_camera);

        wsManager = new WSManager(this, server);
        imageSelecting = false;
        camera = MyCameraManager.getCameraInstance();
        if (camera != null) {
            //camera preview
            cPreview = new CameraPreview(this, camera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cPreview);
            //region view
            regionView = (RegionSelectionView) this.findViewById(R.id.region_view);
            regionView.init(this);

            // initialize Camera manager
            cameraManager = new MyCameraManager(this, camera, cPreview, regionView, wsManager);
            cameraManager.initialize(server, this, this);
            //menu view
            menuView = (MenuView) this.findViewById(R.id.menu_view);
            // selected view
            selectView = (LinearLayout) this.findViewById(R.id.selected_view);
            selectImageView = (ImageView) this.findViewById(R.id.selected_image_view);

            // result event manager
            resultViewManager = new UITResultViewManager(this);

            MenuView menuView = (MenuView) this.findViewById(R.id.menu_view);
            menuView.initializeCameraListener(this);

            int x = getScreenSize().x;
            regionView.setWidthLimited(x - menuView.getLayoutParams().width);
        } else {
            ErrorDialog.newInstance("Cannot use camera !").show(getFragmentManager(),"error");
        }
    }

    @Override
    protected void onStop() {
        Log.d("TAG_DEBUG", "activity stop! ");
        if (regionView!= null && regionView.isScanning()) {
            regionView.stopScan();
        }
        if (wsManager != null) {
            wsManager.cancelExecute();
        }
        super.onStop();
//        if (!imageSelecting && !accessTokenGetting) {
//            releaseCamera();
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("TAG_DEBUG","Activity restarted!");
        //camera.startPreview();
        // camera = MyCameraManager.getCameraInstance();
        if (imageSelecting) {

        } else {
            onCompleteAction();
        }
    }

    @Override
    protected void onDestroy() {
        Process.killProcess(Process.myPid());
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

    void hideMenu() {
        menuView.hideMenu();
        menuView.setTouchEnabled(false);
    }

    void showMenu() {
        if (menuView!= null) {
            menuView.showMenu();
            menuView.setTouchEnabled(true);

        }
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
        if (wsManager != null) {
            wsManager.cancelExecute();
        }
        if (accessTokenGetting) {
            accessTokenGetting = false;
            googleAccountManager.cancelExecute();
            showServerSelectionDialog();
        } else if (regionView != null && regionView.isScanning()) {
                regionView.stopScan();
        } else if (regionView != null && regionView.isRegionSelected()) {
                regionView.closeRegion();
        } else if (resultViewManager != null && resultViewManager.isShown()) {
                this.onCompleted();
        } else { // exit
            long time = System.currentTimeMillis();
            if (time - lastBackPressed > PRESS_DELAY) {
                lastBackPressed = time;
                Toast.makeText(this, "Press back again to exit!",Toast.LENGTH_SHORT).show();
            } else {
                Process.killProcess(Process.myPid());
            }
        }

    }

    @Override
    public void onRegionSelected(Rect regionRect, MotionEvent e) {
        cameraManager.setRegionSelected(regionRect);
    }

    @Override
    public void onRegionConfirmed(Bitmap croppedImage) {
        queryImage = croppedImage;
        resultViewManager.setQueryImage(croppedImage);

    }

    @Override
    public void onRegionCancelScan() {
        if (imageSelecting) {
            selectView.setVisibility(View.INVISIBLE);
            imageSelecting = false;
        }
        showMenu();
        startCameraPreview();
        cameraManager.resumeFlash();
    }

    @Override
    public void onQuerying() {
        regionView.startScan();
        regionView.setSelectEnabled(false);
        hideMenu();
        camera.stopPreview();
        Toast.makeText(this, "Analyzing... Press back to cancel.", Toast.LENGTH_SHORT).show();
    }

    public void startCameraPreview() {
        if (camera != null) {
            try {
                camera.startPreview();
            } catch (Exception e) {
                ErrorDialog.newInstance("Cannot use camera").show(getFragmentManager(),"error");
            }
        }
    }

    public void onCompleteAction() {
        if (!isShowResult) {
            Log.d("TAG_DEBUG","Complete ...!");
            if (regionView!= null && regionView.isScanning()) {
                regionView.stopScan();
            }
            startCameraPreview();
            if (cameraManager != null) cameraManager.resumeFlash();
            showMenu();
            if (regionView != null) regionView.setSelectEnabled(true);
        }
    }

    @Override
    public void onCompleted() {
        if (resultViewManager != null)
            resultViewManager.hideResultView();
        isShowResult = false;
        onCompleteAction();
        //resultViewManager.clearResults();
        //regionView.setVisibility(View.VISIBLE);
    }


    // GOOGLE listener
    @Override
    public void onCloudVisionRespond(BatchAnnotateImagesResponse response) {
        if (response != null) {
            Toast.makeText(this, "GOOGLE RESPOND!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, GoogleVisionResultActivity.class);
            GoogleVisionResultData data = GoogleVisionResultData.getRespondData(response);
            intent.putExtra(GoogleVisionResultActivity.TAG_RESULT_DATA,data);
            intent.putExtra(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING,
                    ImageTools.encodeBitmapToString(queryImage));

            startActivityForResult(intent, REQUEST_SHOW_RESULT);

            isShowResult = true;

            onRespondAction();
        } else {
            onCompleteAction();
        }
    }

    public void onRespondAction() {
        regionView.stopScan();
        if (imageSelecting) {
            selectView.setVisibility(View.INVISIBLE);
            imageSelecting = false;
        }
        hideMenu();
        //camera.startPreview();
    }

    // UIT Web Service listener
    @Override
    public void onServerRespond(String[] rankedList) {
        onRespondAction();
        resultViewManager.clearResults();
        resultViewManager.setResultRankedList(rankedList); // set ranked list
        resultViewManager.showResultView();
    }

    @Override
    public void onRequestImage(String requestTag, String[] imageIds) {
        //resultViewManager.setItemClickEnabled(false);
        //resultViewManager.getQueryTextView().setText(R.string.loading);
        wsManager.executeUITImageRequest(requestTag, imageIds);
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
                        img = MyCameraManager.scaleBitmap(img);
                        selectView.setVisibility(View.VISIBLE);
                        selectImageView.setImageBitmap(img);

                        regionView.setRegion(ViewTools.getImageRectInView(selectImageView));

                        if (server instanceof UITImageRetrievalServer) { // for UIT server
                            img = MyCameraManager.scaleBitmap(img);
                            wsManager.executeUITQueryRequest(img);
                        } else { // for GOOGLE server
                            wsManager.executeGoogleVisionImageRequest(img);
                        }
                        queryImage = img;
                        this.onQuerying();
                        this.onRegionConfirmed(img);
                        //camera.release();
                        //selectedImageView.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(this,"Error when loading image",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                break;
            case REQUEST_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    String email = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    AccountManager am = AccountManager.get(this);
                    // MUST check permission for android 6
                    try {
                        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                        for (Account a : accounts) {
                            if (a.name.equals(email))  {
                                connectGoogleAccount(a);
                                return;
                            }
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(this, "PLEASE SELECT AN ACCOUNT AGAIN !", Toast.LENGTH_SHORT).show();
                pickAccount();
            case REQUEST_SHOW_RESULT:
                isShowResult = false;
                break;
            default:
                return;
        }
    }

    public void selectAnImage() {
        imageSelecting = true;
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK).setType("image/*");
        this.startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
    }

    public Point getScreenSize() {
        Display p = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        p.getSize(size);
        return size;
    }
}
