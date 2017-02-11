package com.uit.instancesearch.camera.listener;

import org.json.JSONObject;

/**
 * Created by m on 23/01/2017.
 */

public interface GoogleVisionRequestListener {
    void onCloudVisionRespond(JSONObject response);
}