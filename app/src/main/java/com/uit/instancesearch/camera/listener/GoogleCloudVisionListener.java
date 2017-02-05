package com.uit.instancesearch.camera.listener;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;

import org.json.JSONObject;

/**
 * Created by m on 15/01/2017.
 */

public interface GoogleCloudVisionListener extends ServiceListener {
    void onCloudVisionRespond(BatchAnnotateImagesResponse response);
}