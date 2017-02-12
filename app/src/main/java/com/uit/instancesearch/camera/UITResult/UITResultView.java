package com.uit.instancesearch.camera.UITResult;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by air on 2/12/17.
 */

public interface UITResultView {
    void initViews(Activity parent);
    void invalidateResult();

    void setQueryImage(Bitmap img);
    void setPreview(int position);

    void showResultMenu();
    void hideResultMenu();
    boolean isShown();

    void clearResults();
    void setResultRankedList(String[] rankedList);
    void setResultImage(String tag, String imgId, Bitmap result);
    void showResultView();
    void hideResultView();

    Context getContext();
}
