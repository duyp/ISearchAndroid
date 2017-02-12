package com.uit.instancesearch.camera.main;

import android.accounts.Account;
import android.graphics.Bitmap;
import android.hardware.Camera;

import com.uit.instancesearch.camera.listener.ActionListener;
import com.uit.instancesearch.camera.listener.RegionSelectListener;

/**
 * Created by air on 2/12/17.
 */

public interface MainController {
    boolean initCamera();
    void init();

    void initUITServer();
    void initGoogleServer(String accessToken);
    int getSelectedServer();
    boolean isGoogleConnected();
    void connectGoogleAccount(Account account);

    void executeQueryRequest(Bitmap img);
    void cancelAllExecution();
    void startCameraPreview();

    void cameraCapture();
    void setCameraFlash(boolean on);

    boolean isUITServerSelected();

    Camera getCameraInstance();
    RegionSelectListener getRegionListener();
    ActionListener getActionListener();
}
