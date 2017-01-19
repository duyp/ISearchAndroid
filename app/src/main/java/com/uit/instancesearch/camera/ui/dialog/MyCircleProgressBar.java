package com.uit.instancesearch.camera.ui.dialog;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by m on 19/01/2017.
 */

public class MyCircleProgressBar {

    private ProgressDialog pDialog;
    private int pStatus = 0;

    public MyCircleProgressBar(Context context, String message, boolean cancelable) {
        pDialog = new ProgressDialog(context);
        pDialog.setCancelable(cancelable);
        pDialog.setMessage(message);
        pDialog.setProgress(0);
        pDialog.setMax(100);
    }




}
