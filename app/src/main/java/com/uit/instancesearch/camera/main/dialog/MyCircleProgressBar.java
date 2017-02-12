package com.uit.instancesearch.camera.main.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

/**
 * Created by m on 19/01/2017.
 */

public class MyCircleProgressBar {

    private static final int SLEEP_TIME = 50; // ms
    private static final int JUMP = 2; // percent

    private static ProgressDialog pDialog;
    private static Thread thread;
    private static final Handler pHandler = new Handler();
    private static int pStatus = 0;

    private static boolean running = false;


    public static void showWithPercent(Context context, String message) {
        pDialog = new ProgressDialog(context);
        pDialog.setProgress(0);
        pDialog.setMax(100);
        pStatus = 0;
        running = true;
        pDialog.setMessage(message);
        pDialog.show();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    if (pStatus <= 100) {
                        pHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                pDialog.setProgress(pStatus);
                            }
                        });
                    }
                    else { pStatus = 0; }
                    pStatus += JUMP;
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        });
        thread.start();
    }

    public static ProgressDialog newDialogInstance(Context c) {
        ProgressDialog dialog = new ProgressDialog(c);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    public static void show(Context context, String message) {
        if (pDialog == null) pDialog = newDialogInstance(context);
        pDialog.setMessage(message);
        try {
            pDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismiss() {
        running = false;
        if (pDialog != null) pDialog.dismiss();
        pDialog = null;
    }

}
