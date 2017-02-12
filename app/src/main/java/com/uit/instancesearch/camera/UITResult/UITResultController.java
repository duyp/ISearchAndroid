package com.uit.instancesearch.camera.UITResult;

import android.graphics.Bitmap;

/**
 * Created by air on 2/12/17.
 */

public interface UITResultController {
    ResultManager getResultManager();
    void getMoreImage();
    void downloadImage(int position);
    void requestPreviewImage(int position);
    void setResultRankedList(String[] rankedList);
    void setResultImage(String tag, String imgId, Bitmap result);
}
