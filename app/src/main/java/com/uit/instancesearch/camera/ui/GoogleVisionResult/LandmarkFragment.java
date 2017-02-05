package com.uit.instancesearch.camera.ui.GoogleVisionResult;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.LandmarkItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;

/**
 * Created by m on 21/01/2017.
 */

public class LandmarkFragment extends Fragment {

    static final String TAG_LANDMARK = "landmark";
    public static final String TAG_INDEX = "index";

    ArrayList<LandmarkItemFragment> itemFragments;

    public LandmarkFragment() {
    }

    public static LandmarkFragment newInstance(String queryImg, ArrayList<LandmarkItem> landmarks) {
        Bundle args = new Bundle();

        args.putString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelableArrayList(TAG_LANDMARK, landmarks);

        LandmarkFragment fragment = new LandmarkFragment();
        fragment.setArguments(args);
        fragment.initItemFragments(landmarks);
        return fragment;
    }

    public void initItemFragments(ArrayList<LandmarkItem> landmarkItems) {
        itemFragments = new ArrayList<>();
        int n = landmarkItems.size();
        if (landmarkItems != null && n > 0)

            for(int i = 0; i < n; i++) {
                LandmarkItem item = landmarkItems.get(i);
                LandmarkItemFragment fragment = LandmarkItemFragment.newInstance(item, i);
                itemFragments.add(fragment);
            }
    }

    public void startProgressBarAnimation() {
        for (LandmarkItemFragment f : itemFragments) {
            f.startAnimation();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("TAG_DEBUG","LANDMARK FRAGMENT: destroyed");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_landmark, container, false);
        Log.d("TAG_DEBUG","LANDMARK FRAGMENT: adding content");

        Bundle args = getArguments();

        DrawableImageView imgView = (DrawableImageView) rootView.findViewById(R.id.landmarkImageView);
        String imgString = args.getString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING);
        if (imgString!=null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));

        ArrayList<LandmarkItem> items = args.getParcelableArrayList(TAG_LANDMARK);
        for (LandmarkItem item : items) {
            imgView.addRectangle(item.points);
        }

        imgView.drawRectangles(DrawableImageView.COLOR_DEFAULT, DrawableImageView.RECT_DEFAULT_STROKE);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        int containerId = R.id.landmarkData;

        if (itemFragments != null) {
            Log.d("TAG_DEBUG", "Fragments != null");
            for (Fragment fragment : itemFragments) {
                if (!fragment.isAdded()) {
                    ft.add(containerId, fragment);
                } else {
                    Log.d("TAG_DEBUG", "Fragment visible is " + fragment.isVisible());
                    if (!fragment.isVisible()) {
                        ft.show(fragment).commit();
                    }
                }
            }
            ft.commit();
        } else {
            Log.d("TAG_DEBUG", "Fragments null");
        }

        return rootView;
    }

    public static class LandmarkItemFragment extends Fragment {

        public static final String TAG_LANDMARK_ITEM = "landmark_item";
        public static final String TAG_LISTENER = "listener";

        public LandmarkItemFragment() {

        }
        public static LandmarkFragment.LandmarkItemFragment newInstance(LandmarkItem item, int index) {
            LandmarkFragment.LandmarkItemFragment fragment = new LandmarkFragment.LandmarkItemFragment();
            Bundle args = new Bundle();
            args.putParcelable(TAG_LANDMARK_ITEM,item);
            args.putInt(TAG_INDEX, index);
            fragment.setArguments(args);

            return fragment;
        }

        public void startAnimation() {
            ProgressBar pb = (ProgressBar)getView().findViewById(R.id.landmarkProgressBar);
            ObjectAnimator animation = ObjectAnimator.ofInt(pb,"progress",0,pb.getProgress());
            animation.setDuration(1000);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_landmark_item,container,false);

            final LandmarkItem item = getArguments().getParcelable(TAG_LANDMARK_ITEM);
            final int index = getArguments().getInt(TAG_INDEX);

            // setting value
            int score = (int)(item.score*100);
            TextView scoreTextView = (TextView)rootView.findViewById(R.id.landmarkScore);
            scoreTextView.setText(String.valueOf(score) + "%");

            TextView descripTextView = (TextView)rootView.findViewById(R.id.landmarkDescription);
            descripTextView.setText(item.description);

            ProgressBar progressBar = (ProgressBar)rootView.findViewById(R.id.landmarkProgressBar);
            progressBar.setProgress(score);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GoogleVisionResultActivity)getActivity())
                            .onHighlight(LandmarkFragment.class, index);
                }
            };

            descripTextView.setOnClickListener(onClickListener);
            scoreTextView.setOnClickListener(onClickListener);
            progressBar.setOnClickListener(onClickListener);

            TextView locationTextView = (TextView)rootView.findViewById(R.id.landmarkLocation);
            locationTextView.setText("Location: " + item.latitude + "," + item.longitude
                                        + "\n(Click to open Google maps)");

            // open Google Map when user click here
            locationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG_DEBUG","Opening google maps...");
                    openGoogleMap(item.latitude, item.longitude, item.description);
                }
            });

            return rootView;


        }

        private void openGoogleMap(double latitude, double longitude, String locationName) {
            GoogleVisionResultActivity.onLandmarkClickOpenGoogleMap(getActivity(),latitude, longitude, locationName);
        }
    }

}
