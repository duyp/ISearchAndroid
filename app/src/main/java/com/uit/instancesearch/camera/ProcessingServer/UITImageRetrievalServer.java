package com.uit.instancesearch.camera.ProcessingServer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.uit.instancesearch.camera.listener.UITWebServiceListener;
import com.uit.instancesearch.camera.tools.ImageTools;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by m on 15/01/2017.
 */

public class UITImageRetrievalServer extends ProcessingServer {
    public static final String TAG_QUERY = "query";

    public static final String TAG_GET_FULL_IMAGE = "get-full";
    public static final String TAG_GET_PREVIEW_IMAGE = "get-preview";
    public static final String TAG_GET_THUMBNAIL_IMAGE = "get-thumbnail";

    //private static final String URL = "http://phamduy.ddns.net:8080/InstanceSearch/services/ISService?wsdl";
    private static final String URL = "http://192.168.1.123:8080/ISearchServices/services/ISService?wsdl";
    //private static final String URL = "http://192.168.24.59:8080/InstanceSearch/services/ISService?wsdl";
    public static final String NAMESPACE = "http://services.instancesearch.uit.com";
    public static final String SOAP_ACTION_PREFIX = "/";

    private static final String METHOD = "clientQueryRequest";
    private static final String METHOD_TEST = "testConnection";

    public static final int MAX_IMAGE_PER_REQUEST = 9;

    UITWebServiceListener wsListener;
    Context context;
    ServiceRunner runner;
    boolean cancelled = true;

    public UITImageRetrievalServer(Context c, UITWebServiceListener listener) {
        context = c;
        this.wsListener = listener;
    }

    @Override
    public void executeQueryRequest(Bitmap bm) {
        cancelled = false;
        String image = ImageTools.encodeBitmapToString(bm);
        runner = new ServiceRunner("Xperia Z", image, TAG_QUERY);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            runner.execute();
        }
    }

    public void executeImageRequest(String requestTag, String[] imageIds) {
        //Log.d("debug", "Request image: "+imageId);
        String requestContent = "";
        for (int i = 0; i < imageIds.length - 1; i++) {
            requestContent += imageIds[i] + ",";
        }
        requestContent += imageIds[imageIds.length - 1];

        runner = new ServiceRunner("Xperia Z", requestContent, requestTag); // test
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            runner.execute();
        }
    }

    @Override
    public void cancelExecute() {
        cancelled = true;
        if (runner!=null) runner.cancel(true);
    }

    private class ServiceRunner extends AsyncTask<String, String, String> {

        private String name;

        private String requestContent;
        private String requestTag;

        protected ServiceRunner(String name, String requestContent, String requestTag) {
            this.name = name;
            this.requestContent = requestContent;
            this.requestTag = requestTag;
        }

        @Override
        protected String doInBackground(String... params) {
            //publishProgress(new String[] {"info", "Uploading image..."});

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            SoapObject request = new SoapObject(NAMESPACE,METHOD);

            request.addProperty("name", name);
            request.addProperty("requestTag",requestTag);
            request.addProperty("queryImageContent",requestContent);

            // test connection
//				SoapObject request = new SoapObject(NAMESPACE,METHOD_TEST);
//				request.addProperty("input", "Xperia Z test connection");

            envelope.bodyOut = request;

            HttpTransportSE transport = new HttpTransportSE(URL);
            long time = System.currentTimeMillis();
            try {
                transport.call(NAMESPACE+SOAP_ACTION_PREFIX+METHOD, envelope);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                publishProgress(new String[] {"err", "Connection problem. Please try again!"});
            } catch (IOException e) {
                publishProgress(new String[] {"err", "Connection problem. Please try again!"});
            }

            if (envelope.bodyIn != null && !this.isCancelled()) {
                // calculate respond time
                time = System.currentTimeMillis() - time;
                //publishProgress(new String[] {"info","Response Time: " + time + "ms"});
                SoapObject resultSoap;
                try {
                    resultSoap = (SoapObject) envelope.bodyIn;
                } catch (Exception e) {
                    publishProgress(new String[] {"err", "Problem parsing result!"});
                    e.printStackTrace();
                    return null;
                }
                    // if error
                if (resultSoap.getProperty(0).toString().equals("err")) {
                    publishProgress(new String[] {"err", "Tag: " + requestTag + " " +resultSoap.getProperty(1).toString()});
                } else {
                    if (requestTag.equals(TAG_QUERY)) {
                        //publishProgress(new String[] {"info","Respone Time: " + time + "ms"});
                        int count = resultSoap.getPropertyCount();
                        if (count < 1) {
                            publishProgress(new String[] {"err", "Processing server error"});
                        } else if (count == 1) { //test connection
                            //publishProgress(new String[] {"info", "Respond: " + resultSoap.getProperty(0).toString()});
                        }
                        else {
                            int nRankedList = Integer.valueOf(resultSoap.getProperty(0).toString());
                            if (nRankedList == 0) {
                                publishProgress(new String[] {"err","No result found!"});
                            } else {
                                String[] rankedList = new String[nRankedList];
                                for(int i = 1; i <= nRankedList; i++) {
                                    rankedList[i-1] = resultSoap.getProperty(i).toString();
                                }
                                publishProgress(rankedList);
                                for(int i = nRankedList + 1; i < count; i++) {
                                    // tag, imageId, image content
                                    publishProgress(new String[] {"image",requestTag,rankedList[i-nRankedList-1],resultSoap.getProperty(i).toString()});
                                }
                            }
                        }
                    } else if (requestTag.equals(TAG_GET_PREVIEW_IMAGE) || requestTag.equals(TAG_GET_FULL_IMAGE)){ // preview || full image
                        // tag, imageId, image content
                        publishProgress(new String[] {"image",requestTag,requestContent,resultSoap.getProperty(0).toString()});
                    } else { // thumbnails
                        int count = resultSoap.getPropertyCount();
                        String[] ids = requestContent.split(",");
                        for (int i = 0; i < count; i++) {
                            publishProgress(new String[] {"image",requestTag,ids[i],resultSoap.getProperty(i).toString()});
                        }
                    }
                }
            } else {
                if (!isCancelled()) publishProgress(new String[]{"err", "Server not responding. Please try again!"});
            }
            return "OK";
        }

        @Override
        protected void onProgressUpdate(String... s) {
            if (this.isCancelled()) return;

            String action = s[0];
            if (action.equals("image")) {
                byte[] b = Base64.decode(s[3], Base64.DEFAULT);
                Bitmap result = BitmapFactory.decodeByteArray(b, 0, b.length);
                wsListener.onImageReceived(s[1], s[2], result);
            } else if (action.equals("info")){
                Toast.makeText(context, "info:" + s[1], Toast.LENGTH_SHORT).show();
                Log.w("TAG_UIT_SERVER",s[1]);
            } else if (action.equals("err")) {
                Toast.makeText(context, "error: " + s[1], Toast.LENGTH_SHORT).show();
                wsListener.onConnectionError();
            } else { // ranked list
                wsListener.onServerRespond(s);
            }
        }
    }
}
