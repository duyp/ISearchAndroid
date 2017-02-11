package com.uit.instancesearch.camera.GoogleResult;

import android.os.Bundle;

import com.uit.instancesearch.camera.GoogleModels.FaceItem;
import com.uit.instancesearch.camera.GoogleModels.GoogleVisionResultData;
import com.uit.instancesearch.camera.GoogleModels.LogoItem;
import com.uit.instancesearch.camera.GoogleModels.SafeSearchItem;

import java.util.ArrayList;

/**
 * Created by air on 2/11/17.
 */

public interface GoogleResultController {
    void requestAdditionData(String queryImage);
    void setResultData(GoogleVisionResultData data);
    void addFaceData(ArrayList<FaceItem> faces);
    void addLogoData(ArrayList<LogoItem> logos);
    void addSafeSearchData(SafeSearchItem safesearch);
    void saveInstanceState(Bundle savedInstanceState);
    GoogleVisionResultData getResultData();
}