package com.uit.instancesearch.camera.manager;

import com.uit.instancesearch.camera.ProcessingServer.ProcessingServer;
import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.listener.UITWebServiceListener;

import android.content.Context;
import android.graphics.Bitmap;

public class WSManager {

	public static int SERVER_UIT = 123;
	public static int SERVER_GOOGLE = 321;

	Context context;
	
	boolean cancelled;

	ProcessingServer server;
	
	public WSManager(Context c, ProcessingServer sv){
		context = c;
		cancelled = false;
		server = sv;
	}

	public void executeUITQueryRequest(UITWebServiceListener wsListener, Bitmap bm) {
		if (server instanceof  UITImageRetrievalServer) {
			((UITImageRetrievalServer)server).executeQueryRequest(bm);
		}
	}

	public void executeUITImageRequest(UITWebServiceListener wsListener, String requestTag, String[] imageIds) {
		if (server instanceof  UITImageRetrievalServer) {
			((UITImageRetrievalServer)server).executeImageRequest(requestTag,imageIds);
		}
	}
	
	public void cancelExecute() {
		if (server != null)
			server.cancelExecute();
	}

}
