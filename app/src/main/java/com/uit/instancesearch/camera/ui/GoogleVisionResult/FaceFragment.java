package com.uit.instancesearch.camera.ui.GoogleVisionResult;

import android.hardware.camera2.params.Face;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.FaceItem;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.SafeSearchItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;

/**
 * Created by m on 24/01/2017.
 */

public class FaceFragment extends Fragment {

    static final String TAG_FACE = "face";
    static final String TAG_INDEX = "index";

    public FaceFragment() {

    }

    public static FaceFragment newInstance(String queryImg, ArrayList<FaceItem> faces) {
        Bundle args = new Bundle();

        args.putString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelableArrayList(TAG_FACE, faces);

        FaceFragment fragment = new FaceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_face, container, false);

        Bundle args = getArguments();

        String imgString = args.getString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING);
        FaceDrawableImageView imgView = (FaceDrawableImageView) rootView.findViewById(R.id.faceImageView);
        if (imgString!=null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));

        if (savedInstanceState == null) {
            ArrayList<FaceItem> items = args.getParcelableArrayList(TAG_FACE);
            FragmentManager fm = getFragmentManager();
            // adding item
            for (int i = 0; i < items.size(); i++) {
                FaceItem item = items.get(i);
                FaceItemFragment fragment = FaceItemFragment.newInstance(item, i);
                fm.beginTransaction().add(R.id.faceData, fragment).commit();
                imgView.addFace(item);
            }
            imgView.drawFaces();
        } else {
        }
        return rootView;
    }

    public static class FaceItemFragment extends Fragment {

        public static final String TAG_FACE_ITEM = "face_item";
        public FaceItemFragment() {

        }
        public static FaceItemFragment newInstance(FaceItem item, int index) {
            FaceItemFragment fragment = new FaceItemFragment();
            Bundle args = new Bundle();
            args.putParcelable(TAG_FACE_ITEM,item);
            args.putInt(TAG_INDEX, index);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_face_item,container,false);

            final FaceItem item = getArguments().getParcelable(TAG_FACE_ITEM);
            if (item != null) {
                int index = getArguments().getInt(TAG_INDEX);

                ((TextView) rootView.findViewById(R.id.faceNumTextView)).setText("Face " + (index+1));
                //  value
                ((TextView) rootView.findViewById(R.id.faceJoyTextView)).setText(item.joy);
                ((TextView) rootView.findViewById(R.id.faceSorrowTextView)).setText(item.sorrow);
                ((TextView) rootView.findViewById(R.id.faceAngerTextView)).setText(item.anger);
                ((TextView) rootView.findViewById(R.id.faceBlurredTextView)).setText(item.blurred);
                ((TextView) rootView.findViewById(R.id.faceExposedTextView)).setText(item.exposed);
                ((TextView) rootView.findViewById(R.id.faceSurpriseTextView)).setText(item.surprise);
                ((TextView) rootView.findViewById(R.id.faceHeadwearTextView)).setText(item.headwear);

                //progress bar
                ((ProgressBar) rootView.findViewById(R.id.faceJoyProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.joy));
                ((ProgressBar) rootView.findViewById(R.id.faceSorrowProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.sorrow));
                ((ProgressBar) rootView.findViewById(R.id.faceAngerProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.anger));
                ((ProgressBar) rootView.findViewById(R.id.faceBlurredProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.blurred));
                ((ProgressBar) rootView.findViewById(R.id.faceExposedProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.exposed));
                ((ProgressBar) rootView.findViewById(R.id.faceSurpriseProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.surprise));
                ((ProgressBar) rootView.findViewById(R.id.faceHeadwearProgressBar))
                        .setProgress(SafeSearchItem.getScoreOf(item.headwear));

                // Pan, Tilt, Roll
                ((TextView) rootView.findViewById(R.id.faceRollTextView))
                        .setText(getString(R.string.face_roll) + ": " + String.valueOf(item.rollAngle));
                ((TextView) rootView.findViewById(R.id.faceTiltTextView))
                        .setText(getString(R.string.face_tilt) + ": " + String.valueOf(item.tiltAngle));
                ((TextView) rootView.findViewById(R.id.facePanTextView))
                        .setText(getString(R.string.face_pan) + ": " + String.valueOf(item.panAngle));

                // confidence
                ((TextView) rootView.findViewById(R.id.faceDetectionScore))
                        .setText((int)(item.detectionConfidence*100) + "%");
                ((ProgressBar) rootView.findViewById(R.id.faceDetectionProgressBar))
                        .setProgress((int)(item.detectionConfidence*100));

                ((TextView) rootView.findViewById(R.id.faceLandmarkingScore))
                        .setText((int)(item.landmarkingConfidence*100) + "%");
                ((ProgressBar) rootView.findViewById(R.id.faceLandmarkingProgressBar))
                        .setProgress((int)(item.landmarkingConfidence*100));

            }

            return rootView;
        }
    }
}
