package com.uit.instancesearch.camera.ui.GoogleVisionResult;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.LabelItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;

/**
 * Created by m on 21/01/2017.
 */

public class LabelFragment extends Fragment {

    static final String TAG_LABEL = "label";

    public LabelFragment() {

    }

    public static LabelFragment newInstance(String queryImg, ArrayList<LabelItem> labels) {
        Bundle args = new Bundle();

        args.putString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelableArrayList(TAG_LABEL, labels);

        LabelFragment fragment = new LabelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_label, container, false);

        Bundle args = getArguments();

        String imgString = args.getString(GoogleVisionResultActivity.TAG_QUERY_IMAGE_STRING);
        ImageView imgView = (ImageView) rootView.findViewById(R.id.labelImageView);
        if (imgString!=null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));

        if (savedInstanceState == null) {
            //Log.d("TAG_DEBUG","LABEL FRAGMENT: adding content (NULL)");
            ArrayList<LabelItem> items = args.getParcelableArrayList(TAG_LABEL);
            FragmentManager fm = getFragmentManager();
            // adding item
            for (LabelItem item : items) {
                LabelItemFragment fragment = LabelItemFragment.newInstance(item);
                fm.beginTransaction().add(R.id.labelData, fragment).commit();
            }
        } else {
//                Log.d("TAG_DEBUG","LABEL FRAGMENT: adding content (NOT NULL)");
        }
        return rootView;
    }

    public static class LabelItemFragment extends Fragment {

        public static final String TAG_LABEL_ITEM = "label_item";
        public LabelItemFragment() {

        }
        public static LabelItemFragment newInstance(LabelItem item) {
            LabelItemFragment fragment = new LabelItemFragment();
            Bundle args = new Bundle();
            args.putParcelable(TAG_LABEL_ITEM,item);

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_label_item,container,false);

            final LabelItem label = getArguments().getParcelable(TAG_LABEL_ITEM);

            // setting value
            ((TextView)rootView.findViewById(R.id.labelDescription))
                    .setText(label.description);
            int score = (int)(label.score*100);
            ((TextView)rootView.findViewById(R.id.labelScore))
                    .setText(String.valueOf(score) + "%");
            ((ProgressBar)rootView.findViewById(R.id.labelProgressBar)).setProgress(score);

            return rootView;
        }
    }
}
