package com.uit.instancesearch.camera.GoogleResult;

import android.os.Bundle;

import com.uit.instancesearch.camera.GoogleModels.FaceItem;
import com.uit.instancesearch.camera.GoogleModels.GoogleVisionResultData;
import com.uit.instancesearch.camera.GoogleModels.LogoItem;
import com.uit.instancesearch.camera.GoogleModels.SafeSearchItem;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationPostServer;
import com.uit.instancesearch.camera.listener.GoogleVisionRequestListener;
import com.uit.instancesearch.camera.tools.ImageTools;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by air on 2/11/17.
 */

public class GoogleResultControllerImpl implements GoogleResultController,
        GoogleVisionRequestListener{


    GoogleImageAnnotationPostServer server;
    GoogleVisionResultData resultData;

    GoogleResultView viewListener;

    public GoogleResultControllerImpl(GoogleResultView view) {
        viewListener = view;
    }

    @Override
    public void onCloudVisionRespond(JSONObject response) {
        if (response != null) {
            resultData.setLogos(GoogleVisionResultData.getLogoItem(response));
            resultData.setSafeSearch(GoogleVisionResultData.getSafeSearchItem(response));
            resultData.setFaces(GoogleVisionResultData.getFaceItem(response));
        } else {
            //Toast.makeText(this,"Cannot get addition data !", Toast.LENGTH_SHORT).show();
        }
        viewListener.displayResultData();
    }

    @Override
    public void requestAdditionData(String queryImage) {
        server = new GoogleImageAnnotationPostServer(this);
        server.executeQueryRequest(ImageTools.decodeStringToBitmap(queryImage));
    }

    @Override
    public void setResultData(GoogleVisionResultData data) {
        resultData = data;
    }

    @Override
    public void addFaceData(ArrayList<FaceItem> faces) {
        resultData.setFaces(faces);
    }

    @Override
    public void addLogoData(ArrayList<LogoItem> logos) {
        resultData.setLogos(logos);
    }

    @Override
    public void addSafeSearchData(SafeSearchItem safesearch) {
        resultData.setSafeSearch(safesearch);
    }

    @Override
    public void saveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(GoogleResultActivity.TAG_FACE_DATA, resultData.getFaces());
        savedInstanceState.putParcelableArrayList(GoogleResultActivity.TAG_LOGO_DATA, resultData.getLogos());
        savedInstanceState.putParcelable(GoogleResultActivity.TAG_SAFE_SEARCH_DATA, resultData.getSafeSearch());
    }

    @Override
    public GoogleVisionResultData getResultData() {
        return resultData;
    }
}
