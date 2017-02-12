package com.uit.instancesearch.camera.UITResult;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.listener.UITWebServiceListener;
import com.uit.instancesearch.camera.manager.WSManager;
import com.uit.instancesearch.camera.tools.ImageTools;

/**
 * Created by air on 2/12/17.
 */

public class UITResultControllerImpl implements UITResultController, UITWebServiceListener {

    ResultManager manager;
    UITImageRetrievalServer server;
    UITResultView view;

    public UITResultControllerImpl(UITResultView view, String serverIP) {
        this.view = view;
        server = new UITImageRetrievalServer(view.getContext(), serverIP, this);
        manager = new ResultManager();
    }

    @Override
    public ResultManager getResultManager() {
        return manager;
    }

    @Override
    public void getMoreImage() {
        int remaining = manager.getRankedListCount() - manager.getThumbnailCount();
        int nImage = remaining;
        if (remaining > UITImageRetrievalServer.MAX_IMAGE_PER_REQUEST) {
            nImage = UITImageRetrievalServer.MAX_IMAGE_PER_REQUEST;
        }
        manager.setLoadingThumbCount(nImage);
        // request images
        String[] imageIds = new String[nImage];
        for(int i = 0; i < nImage; i++) {
            imageIds[i] = manager.getRankAt(manager.getThumbnailCount() + i);
        }
        server.executeImageRequest(UITImageRetrievalServer.TAG_GET_THUMBNAIL_IMAGE, imageIds);
    }

    @Override
    public void downloadImage(int position) {
        String imgId = manager.getImageId(position);
        if (!manager.isDownloaded(imgId)) {
            server.executeImageRequest(UITImageRetrievalServer.TAG_GET_FULL_IMAGE, new String[]{imgId});
            Toast.makeText(view.getContext(), "Downloading image ...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Just downloaded !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void requestPreviewImage(int position) {
        String[] imageId = new String[] {manager.getImageId(position)};
        server.executeImageRequest(UITImageRetrievalServer.TAG_GET_PREVIEW_IMAGE, imageId);
    }

    @Override
    public void onServerRespond(String[] rankedList) {

    }

    @Override
    public void setResultRankedList(String[] rankedList) {
        manager.clearResult();
        manager.setRankedList(rankedList); // set ranked list
        view.showResultView();
    }

    @Override
    public void onImageReceived(String tag, String imgId, Bitmap result) {
        if (tag.equals(UITImageRetrievalServer.TAG_QUERY) ||
                tag.equals(UITImageRetrievalServer.TAG_GET_THUMBNAIL_IMAGE)) {

            manager.setThumbnail(imgId, result); // add thumbnail with image id
            view.invalidateResult();
        } else if (tag.equals(UITImageRetrievalServer.TAG_GET_PREVIEW_IMAGE)) {

            manager.setPreview(imgId, result);
            view.invalidateResult();

        } else if (tag.equals(UITImageRetrievalServer.TAG_GET_FULL_IMAGE)) {
            // save image
            ImageTools.saveImage(result);
            Toast.makeText(view.getContext(), "Image " + imgId + ": saved to downloads directory !",
                    Toast.LENGTH_SHORT).show();
            manager.setDownloaded(imgId);
        }
    }

    @Override
    public void setResultImage(String tag, String imgId, Bitmap result) {
        onImageReceived(tag, imgId, result);
    }

    @Override
    public void onConnectionError() {

    }
}
