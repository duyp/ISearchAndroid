package com.uit.instancesearch.camera.ProcessingServer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.uit.instancesearch.camera.listener.GoogleCloudVisionRequestListener;
import com.uit.instancesearch.camera.tools.ImageTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by m on 23/01/2017.
 */

public class GoogleImageAnnotationPostServer extends ProcessingServer{

    public final static String NAME_IMAGE = "image";
    public final static String NAME_IMAGE_CONTENT = "content";

    public final static String NAME_FEATURE = "features";
    public final static String NAME_FEATURE_TYPE = "type";

    public final static String NAME_ROOT = "requests";

    private final static String REQUEST_URL = "https://vision.googleapis.com/v1/images:annotate?key=AIzaSyDM9mCLluCM-jUb6FqRMugYB4-sLwiTBV8";

    CloudVisionRequestRunner requestRunner; // using HTTP POST request
    GoogleCloudVisionRequestListener listener;

    public GoogleImageAnnotationPostServer(GoogleCloudVisionRequestListener gl) {
        listener = gl;
    }

    @Override
    public void executeQueryRequest(Bitmap bm) {
        requestRunner = new CloudVisionRequestRunner(bm);
        requestRunner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void onRespond(JSONObject obj) {
        listener.onCloudVisionRespond(obj);
        requestRunner = null;
    }

    @Override
    public void cancelExecute() {
        if (requestRunner != null) {
            requestRunner.cancel(true);
        }
    }


    private class CloudVisionRequestRunner extends AsyncTask<Void, Void, JSONObject> {

        Bitmap requestImage;

        public CloudVisionRequestRunner(Bitmap requestImage) {
            this.requestImage = requestImage;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                java.net.URL url = new URL(REQUEST_URL);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                try {
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestMethod("POST");

                    JSONObject requestJsonObject = createJsonObjectRequest(ImageTools.encodeBitmapToString(requestImage));

                    OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                    os.write(requestJsonObject.toString().getBytes("UTF-8"));
                    os.close();

                    // getting response
                    StringBuilder sb = new StringBuilder();
                    int httpResult = connection.getResponseCode();
                    if (httpResult == HttpURLConnection.HTTP_OK) {
                        InputStream is = new BufferedInputStream(connection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                        String line = null;
                        while((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        reader.close();
                        //Log.d("TAG_DEBUG_REQUEST", sb.toString());
                    }
                    return new JSONObject(sb.toString());
                } finally {
                    connection.disconnect();
                }
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            onRespond(result);
        }
    }

    public static JSONObject createJsonObjectRequest(String base64EncodedImage) {
        JSONObject requests = new JSONObject();

        JSONObject root = new JSONObject();
        JSONObject imageObj = new JSONObject();

        JSONArray features = new JSONArray();

        try {
            imageObj.put(NAME_IMAGE_CONTENT, base64EncodedImage);
            requests.put(NAME_IMAGE, imageObj);

            // request logo detection
            JSONObject featureObj = new JSONObject();
            featureObj.put(NAME_FEATURE_TYPE, GoogleImageAnnotationServer.TYPE_LOGO_DETECTION);
            features.put(featureObj);

            // request safe search detection
            featureObj = new JSONObject();
            featureObj.put(NAME_FEATURE_TYPE, GoogleImageAnnotationServer.TYPE_SAFE_SEARCH_DETECTION);
            features.put(featureObj);

            // request face detection
            featureObj = new JSONObject();
            featureObj.put(NAME_FEATURE_TYPE, GoogleImageAnnotationServer.TYPE_FACE_DETECTION);
            features.put(featureObj);

            requests.put(NAME_FEATURE, features);

            root.put(NAME_ROOT, requests);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return root;
    }

}
