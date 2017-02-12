package com.uit.instancesearch.camera.main;

import android.accounts.Account;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationServer;
import com.uit.instancesearch.camera.ProcessingServer.ProcessingServer;
import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.listener.ActionListener;
import com.uit.instancesearch.camera.listener.GoogleCloudVisionListener;
import com.uit.instancesearch.camera.listener.RegionSelectListener;
import com.uit.instancesearch.camera.listener.UITWebServiceListener;
import com.uit.instancesearch.camera.manager.GoogleAccountManager;
import com.uit.instancesearch.camera.manager.MyCameraManager;
import com.uit.instancesearch.camera.manager.WSManager;

/**
 * Created by air on 2/12/17.
 */

public class MainControllerImpl implements MainController,
        UITWebServiceListener,
        GoogleAccountListener,
        GoogleCloudVisionListener,
        RegionSelectListener,
        ActionListener{

    WSManager wsManager;        // web services manager

    int selectedServer;
    ProcessingServer server;
    GoogleAccountManager googleAccountManager;

    MainView mainView;

    MyCameraManager cameraManager;    // camera manager
    Camera camera;            // camera hardware

    public MainControllerImpl(MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public boolean initCamera() {
        camera = MyCameraManager.getCameraInstance();
        return camera != null;
    }

    @Override
    public void init() {
        if (camera!= null) {
            cameraManager = new MyCameraManager(mainView.getContext(),
                    camera,
                    mainView.getCameraPreview(),
                    mainView.getRegionView());
            cameraManager.initialize(server,this,this);
        }
    }

    @Override
    public void initUITServer() {
        this.selectedServer = WSManager.SERVER_UIT;
        server = new UITImageRetrievalServer(mainView.getContext(), this);
    }

    @Override
    public void initGoogleServer(String accessToken) {
        this.selectedServer = WSManager.SERVER_GOOGLE;
        server = new GoogleImageAnnotationServer(this,accessToken);
    }

    @Override
    public int getSelectedServer() {
        return selectedServer;
    }

    @Override
    public void connectGoogleAccount(Account account) {
        googleAccountManager = new GoogleAccountManager(mainView.getContext(), this, account);
        googleAccountManager.getAuthToken();
    }

    @Override
    public boolean isUITServerSelected() {
        return selectedServer == WSManager.SERVER_UIT;
    }

    @Override
    public void executeQueryRequest(Bitmap img) {
        server.executeQueryRequest(img);
    }

    @Override
    public Camera getCameraInstance() {
        return camera;
    }

    @Override
    public RegionSelectListener getRegionListener() {
        return this;
    }

    @Override
    public ActionListener getActionListener() {
        return this;
    }

    @Override
    public void onTokenReceived() {
        String accessToken = googleAccountManager.getAccessToken();
        if(accessToken != null) {
            initGoogleServer(accessToken);
        }
        mainView.onGoogleRespond();
    }

    @Override
    public boolean isGoogleConnected() {
        return googleAccountManager.getAccessToken() != null;
    }
    // UIT Web service listener
    @Override
    public void onServerRespond(String[] rankedList) {
        mainView.onUITServerRespond(rankedList);
    }

    @Override
    public void onImageReceived(String tag, String imgId, Bitmap result) {
        mainView.setResultImage(tag, imgId, result);
    } // not use this

    @Override
    public void onConnectionError() {
        mainView.onConnectionError();
    }


    @Override
    public void onCloudVisionRespond(BatchAnnotateImagesResponse response) {
        mainView.onGoogleCloudVisionRespond(response);
    }

    @Override
    public void setCameraFlash(boolean on) {
        cameraManager.flashChange(on);
    }

    @Override
    public void cameraCapture() {
        cameraManager.capture();
        cameraManager.pauseFlash();
    }

    @Override
    public void onCompleted() {
        mainView.onCompleted();
    }

    @Override
    public void onQuerying() {
        camera.stopPreview();
        mainView.onQuerying();
    }

    // REGION LISTENER
    @Override
    public void onRegionSelected(Rect regionRect, MotionEvent e) {
        cameraManager.setRegionSelected(regionRect);
    }

    @Override
    public void onRegionConfirmed(Bitmap croppedImage) {
        mainView.setQueryImage(croppedImage);
    }

    @Override
    public void onRegionCancelScan() {
        mainView.cancelScan();
        startCameraPreview();
    }

    @Override
    public void cancelAllExecution() {
        if (googleAccountManager!= null)
            googleAccountManager.cancelExecute();
        server.cancelExecute();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    @Override
    public void startCameraPreview() {
        if (camera != null) {
            try {
                camera.startPreview();
                if (cameraManager != null)
                    cameraManager.resumeFlash();
            } catch (Exception e) {
                Toast.makeText(mainView.getContext(), "Cannot use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }
}
