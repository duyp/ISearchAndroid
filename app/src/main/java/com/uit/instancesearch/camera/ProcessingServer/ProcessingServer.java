package com.uit.instancesearch.camera.ProcessingServer;

import android.graphics.Bitmap;

/**
 * Created by m on 15/01/2017.
 */

public abstract class ProcessingServer {

    public abstract void executeQueryRequest(Bitmap bm);
    public abstract void cancelExecute();
}
