package com.uit.instancesearch.camera.ProcessingServer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.uit.instancesearch.camera.listener.GoogleCloudVisionListener;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 15/01/2017.
 */

public class GoogleImageAnnotationServer extends ProcessingServer {

    public final static String TYPE_LABEL_DETECTION = "LABEL_DETECTION";
    public final static String TYPE_TEXT_DETECTION = "TEXT_DETECTION";
    public final static String TYPE_LANDMARK_DETECTION = "LANDMARK_DETECTION";
    public final static String TYPE_LOGO_DETECTION = "LOGO_DETECTION";
    public final static String TYPE_SAFE_SEARCH_DETECTION = "SAFE_SEARCH_DETECTION";
    public final static String TYPE_FACE_DETECTION = "FACE_DETECTION";

    GoogleCloudVisionListener listener;

    GoogleCredential credential;
    JsonFactory jsonFactory;

    CloudVisionRunner runner; // using google library


    public GoogleImageAnnotationServer(GoogleCloudVisionListener gl, String accessToken) {
        credential = new GoogleCredential().setAccessToken(accessToken);
        jsonFactory = GsonFactory.getDefaultInstance();
        listener = gl;
    }

    @Override
    public void executeQueryRequest(Bitmap bm) {
        runner = new CloudVisionRunner(bm);
        runner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onRespond(BatchAnnotateImagesResponse response) {
        listener.onCloudVisionRespond(response);
        runner = null;
    }


    @Override
    public void cancelExecute() {
        if (runner != null) {
            runner.cancel(true);
        }
    }

    private class CloudVisionRunner extends AsyncTask<Void , Void, BatchAnnotateImagesResponse> {

        Bitmap image;

        public CloudVisionRunner(Bitmap bm) {
            image = bm;
        }

        @Override
        protected BatchAnnotateImagesResponse doInBackground(Void... params) {
            try {

                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, credential);
                Vision vision = builder.build();

                List<Feature> featureList = new ArrayList<Feature>();
                featureList.add(new Feature()
                        .setType(TYPE_LABEL_DETECTION)
                        .setMaxResults(10));
                featureList.add(new Feature()
                        .setType(TYPE_TEXT_DETECTION)
                        .setMaxResults(10));
                featureList.add(new Feature()
                        .setType(TYPE_LANDMARK_DETECTION)
                        .setMaxResults(10));

                List<AnnotateImageRequest> imageRequestList = new ArrayList<AnnotateImageRequest>();
                imageRequestList.add(new AnnotateImageRequest()
                        .setImage(ImageTools.getBase64EncodedJpeg(image))
                        .setFeatures(featureList));

                BatchAnnotateImagesRequest batchAnnotateImagesRequest
                        = new BatchAnnotateImagesRequest().setRequests(imageRequestList);


                Vision.Images.Annotate annotate = vision.images().annotate(batchAnnotateImagesRequest);
                // Due to a bug: requests to Vision API containing large images fail when GZipped.
                annotate.setDisableGZipContent(true);
                Log.w("GOOGLE VISION","sending request...");

                // execute request and return response
                return annotate.execute();

            } catch (GoogleJsonResponseException ge){
                ge.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(BatchAnnotateImagesResponse response) {
            onRespond(response);
        }
    }
}
