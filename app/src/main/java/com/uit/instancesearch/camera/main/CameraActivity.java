package com.uit.instancesearch.camera.main;

import java.io.FileNotFoundException;
import java.io.InputStream;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.uit.instancesearch.camera.GoogleModels.GoogleVisionResultData;
import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.GoogleResult.GoogleResultActivity;
import com.uit.instancesearch.camera.UITResult.UITResultView;
import com.uit.instancesearch.camera.UITResult.UITResultViewImpl;
import com.uit.instancesearch.camera.main.dialog.ErrorDialog;
import com.uit.instancesearch.camera.main.dialog.MyCircleProgressBar;
import com.uit.instancesearch.camera.main.dialog.ServerIPDialogFragment;
import com.uit.instancesearch.camera.main.dialog.ServersDialogFragment;
import com.uit.instancesearch.camera.manager.MyCameraManager;
import com.uit.instancesearch.camera.manager.WSManager;
import com.uit.instancesearch.camera.tools.ImageTools;
import com.uit.instancesearch.camera.tools.ViewTools;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity
        implements MainView,
        MainMenuListener,
        ServersDialogFragment.ServersDialogListener,
        ServerIPDialogFragment.ServerIPDialogListener {

    private static final int REQUEST_SELECT_PHOTO = 100;

    public static final int REQUEST_ACCOUNT_AUTHORIZATION = 42;
    private static final int REQUEST_PICK_ACCOUNT = 41;
    private static final int REQUEST_ACCOUNT_PERMISSION = 43;
    private static final int REQUEST_CAMERA_PERMISSION = 44;
    private static final int REQUEST_SHOW_RESULT = 46;


    UITResultView resultView;


    CameraPreview cPreview;        // camera preview
    RegionSelectionView regionView;        // region selection view
    MenuView menuView;              // menu view

    ImageView selectedImageView;
    LinearLayout selectedView;

    boolean imageSelecting = false;
    boolean accessTokenGetting = false;
    boolean isShowResult = false;

    // temp
    Bitmap queryImage;

    long lastBackPressed = -1;
    public static long PRESS_DELAY = 2000; // 2 seconds

    private MainController controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        controller = new MainControllerImpl(this);
        //set content view AFTER ABOVE sequence (to avoid crash)

        // show server selection dialog
        showServerSelectionDialog();
    }

    void showServerSelectionDialog() {
        ServersDialogFragment serverDialog = new ServersDialogFragment();
        serverDialog.setCancelable(false);
        serverDialog.show(getFragmentManager(), "servers");
    }

    void showServerIPDialog() {
        ServerIPDialogFragment serverIPDialog = new ServerIPDialogFragment();
        serverIPDialog.setCancelable(false);
        serverIPDialog.show(getFragmentManager(), "server_ip");
    }

    @Override
    public void onServersDialogPositiveClick(DialogFragment dialog, int chosenServer) {
        if (chosenServer == WSManager.SERVER_UIT) {
            showServerIPDialog();
        } else {
            // check and request Account permission
            checkAccountPermission();
        }
    }

    @Override
    public void onServerIPDialogPositiveClick(String serverIP) {
        controller.initUITServer(serverIP);
        checkCameraPermission();
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
        controller.connectGoogleAccount(a);
        MyCircleProgressBar.show(this,"Requesting Google access token...");
    }

    @Override
    public void onGoogleRespond() {
        accessTokenGetting = false;
        MyCircleProgressBar.dismiss();
        if (controller.isGoogleConnected()) {
            checkCameraPermission();
        } else {
            Toast.makeText(this,"Cannot access to Google server!",Toast.LENGTH_SHORT).show();
            showServerSelectionDialog();
        }
    }

    // initialize necessary parameter
    private void initialize() {
        this.setContentView(R.layout.activity_camera);

        imageSelecting = false;
        if (controller.initCamera()) {
            //camera preview
            cPreview = new CameraPreview(this, controller.getCameraInstance());
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cPreview);

            //region view
            regionView = (RegionSelectionView) this.findViewById(R.id.region_view);
            regionView.init(controller.getRegionListener());

            //menu view
            menuView = (MenuView) this.findViewById(R.id.menu_view);
            int x = getScreenSize().x;
            regionView.setWidthLimited(x - menuView.getLayoutParams().width);

            // selected view
            selectedView = (LinearLayout) this.findViewById(R.id.selected_view);
            selectedImageView = (ImageView) this.findViewById(R.id.selected_image_view);

            if (controller.isUITServerSelected()) {
                // result event manager
                resultView = new UITResultViewImpl(this, UITImageRetrievalServer.serverIP);
            }

            MenuView menuView = (MenuView) this.findViewById(R.id.menu_view);
            menuView.initializeCameraListener(this);

            // initialize controller
            controller.init();
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
        controller.cancelAllExecution();
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

    @Override
    public void onBackPressed() {

        controller.cancelAllExecution();

        if (accessTokenGetting) {
            accessTokenGetting = false;
//            googleAccountManager.cancelExecute();
            showServerSelectionDialog();
        } else if (regionView != null && regionView.isScanning()) {
                regionView.stopScan();
        } else if (regionView != null && regionView.isRegionSelected()) {
                regionView.closeRegion();
        } else if (resultView != null && resultView.isShown()) {
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
    public void setResultImage(String tag, String imgId, Bitmap result) {
        resultView.setResultImage(tag, imgId, result);
    }

    @Override
    public void setQueryImage(Bitmap croppedImage) {
        queryImage = croppedImage;
        if (controller.isUITServerSelected()) {
            resultView.setQueryImage(croppedImage);
        }
    }

    @Override
    public void cancelScan() {
        if (imageSelecting) {
            selectedView.setVisibility(View.INVISIBLE);
            imageSelecting = false;
        }
        showMenu();
    }

    @Override
    public void onQuerying() {
        regionView.startScan();
        regionView.setSelectEnabled(false);
        hideMenu();
        Toast.makeText(this, "Analyzing... Press back to cancel.", Toast.LENGTH_SHORT).show();
    }

    public void onCompleteAction() {
        if (!isShowResult) {
            Log.d("TAG_DEBUG","Complete ...!");
            if (regionView!= null && regionView.isScanning()) {
                regionView.stopScan();
            }
            controller.startCameraPreview();
            showMenu();
            if (regionView != null) regionView.setSelectEnabled(true);
        }
    }

    @Override
    public void onCompleted() {
        if (resultView != null)
            resultView.hideResultView();
        isShowResult = false;
        onCompleteAction();
        //resultViewManager.clearResults();
        //regionView.setVisibility(View.VISIBLE);
    }

    // GOOGLE listener
    @Override
    public void onGoogleCloudVisionRespond(BatchAnnotateImagesResponse response) {
        if (response != null) {
            Intent intent = new Intent(this, GoogleResultActivity.class);
            GoogleVisionResultData data = GoogleVisionResultData.getRespondData(response);
            intent.putExtra(GoogleResultActivity.TAG_RESULT_DATA,data);
            intent.putExtra(GoogleResultActivity.TAG_QUERY_IMAGE_STRING,
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
            selectedView.setVisibility(View.INVISIBLE);
            imageSelecting = false;
        }
        hideMenu();
        //camera.startPreview();
    }

    @Override
    public void onUITServerRespond(String[] rankedList) {
        onRespondAction();
        resultView.clearResults();
        resultView.setResultRankedList(rankedList); // set ranked list
        resultView.showResultView();
    }

    @Override
    public void onConnectionError() {
        if (regionView.isScanning()) {
            regionView.stopScan();
        }
//        if (resultViewManager.isShown()) {
//            //resultViewManager.getQueryTextView().setText(R.string.none);
//            //resultViewManager.setItemClickEnabled(true);
//        }
    }

    // Main Menu actions
    @Override
    public void onCameraFlashChange(boolean on) {
        controller.setCameraFlash(on);
    }

    @Override
    public void onCameraCapture() {
        controller.cameraCapture();
    }

    @Override
    public void onSelectImage() {
        imageSelecting = true;
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK).setType("image/*");
        this.startActivityForResult(photoPickerIntent, REQUEST_SELECT_PHOTO);
    }
    // end main menu actions

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
                        selectedView.setVisibility(View.VISIBLE);
                        selectedImageView.setImageBitmap(img);

                        regionView.setRegion(ViewTools.getImageRectInView(selectedImageView));

                        controller.executeQueryRequest(img);

                        queryImage = img;
                        this.onQuerying();
                        this.setQueryImage(img);
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

    public Point getScreenSize() {
        Display p = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        p.getSize(size);
        return size;
    }

    @Override
    public RegionSelectionView getRegionView() {
        return regionView;
    }

    @Override
    public CameraPreview getCameraPreview() {
        return cPreview;
    }

    @Override
    public Context getContext() {
        return this;
    }

}
