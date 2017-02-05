package com.uit.instancesearch.camera.ui.GoogleVisionResult;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.LabelItem;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.SafeSearchItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;

/**
 * Created by m on 21/01/2017.
 */

public class SafeSearchFragment extends Fragment {

    static final String TAG_SAFESEARCH = "safesearch";

    public SafeSearchFragment() {

    }

    public static SafeSearchFragment newInstance(String queryImg, SafeSearchItem safeSearchItem) {
        Bundle args = new Bundle();

        args.putString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelable(TAG_SAFESEARCH, safeSearchItem);

        SafeSearchFragment fragment = new SafeSearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_safesearch, container, false);

        Bundle args = getArguments();

        String imgString = args.getString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING);
        ImageView imgView = (ImageView) rootView.findViewById(R.id.safesearchImageView);
        if (imgString!=null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));

        if (savedInstanceState == null) {
            //Log.d("TAG_DEBUG","LABEL FRAGMENT: adding content (NULL)");
            SafeSearchItem item = args.getParcelable(TAG_SAFESEARCH);
            // adding item
            if (item != null) {
                int containerId = R.id.safesearchData;
                FragmentTransaction ft = getFragmentManager().beginTransaction();

                Fragment f = SafeSearchItemFragment.newInstance(getString(R.string.safesearch_adult),
                        item.adultValue);
                ft.add(containerId, f);

                f = SafeSearchItemFragment.newInstance(getString(R.string.safesearch_violence),
                        item.violenceValue);
                ft.add(containerId, f);

                f = SafeSearchItemFragment.newInstance(getString(R.string.safesearch_medical),
                        item.medicalValue);
                ft.add(containerId, f);

                f = SafeSearchItemFragment.newInstance(getString(R.string.safesearch_spoof),
                        item.spoofValue);
                ft.add(containerId, f);

                ft.commit();
            }

        } else {
//                Log.d("TAG_DEBUG","LABEL FRAGMENT: adding content (NOT NULL)");
        }
        return rootView;
    }

    public static class SafeSearchItemFragment extends Fragment {

        public static final String TAG_SAFESEARCH_NAME = "name";
        public static final String TAG_SAFESEARCH_VALUE = "value";
        public SafeSearchItemFragment() {

        }
        public static SafeSearchItemFragment newInstance(String name, String value) {
            SafeSearchItemFragment fragment = new SafeSearchItemFragment();
            Bundle args = new Bundle();
            args.putString(TAG_SAFESEARCH_NAME, name);
            args.putString(TAG_SAFESEARCH_VALUE, value);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_safesearch_item,container,false);
            Bundle args = getArguments();
            String name = args.getString(TAG_SAFESEARCH_NAME);
            String value = args.getString(TAG_SAFESEARCH_VALUE);
            // setting value
            ((TextView)rootView.findViewById(R.id.safesearchName))
                    .setText(name);
            ((TextView)rootView.findViewById(R.id.safesearchValue))
                    .setText(value);

            ((ProgressBar)rootView.findViewById(R.id.safesearchProgressBar))
                    .setProgress(SafeSearchItem.getScoreOf(value));

            return rootView;
        }
    }
}
