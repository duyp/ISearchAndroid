package com.uit.instancesearch.camera.main;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;

/**
 * Created by air on 2/12/17.
 */

public interface MainView {
    void setQueryImage(Bitmap img);
    void cancelScan();


    void onGoogleRespond();
    void onQuerying();
    void onCompleted();
    void onGoogleCloudVisionRespond(BatchAnnotateImagesResponse response);
    void onUITServerRespond(String[] rankedList);
    void setResultImage(String tag, String imgId, Bitmap result);
    void onConnectionError();

    RegionSelectionView getRegionView();
    CameraPreview getCameraPreview();
    Context getContext();
}
