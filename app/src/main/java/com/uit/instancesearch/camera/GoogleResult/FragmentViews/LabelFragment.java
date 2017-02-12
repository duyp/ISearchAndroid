package com.uit.instancesearch.camera.GoogleResult.FragmentViews;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uit.instancesearch.camera.GoogleResult.GoogleResultActivity;
import com.uit.instancesearch.camera.GoogleModels.LabelItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by m on 21/01/2017.
 */

public class LabelFragment extends Fragment {

    static final String TAG_LABEL = "label";
    ArrayList<LabelItemFragment> fragments;

    public LabelFragment() {
        fragments = new ArrayList<>();
    }

    public static LabelFragment newInstance(String queryImg, ArrayList<LabelItem> labels) {
        Bundle args = new Bundle();

        args.putString(GoogleResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelableArrayList(TAG_LABEL, labels);

        LabelFragment fragment = new LabelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void startProgressBarAnimation() {
        for (LabelItemFragment f : fragments) {
            f.startAnimation();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_label, container, false);

        Bundle args = getArguments();

        String imgString = args.getString(GoogleResultActivity.TAG_QUERY_IMAGE_STRING);
        ImageView imgView = (ImageView) rootView.findViewById(R.id.labelImageView);
        if (imgString!=null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));

        if (savedInstanceState == null) {
            //Log.d("TAG_DEBUG","LABEL FRAGMENT: adding content (NULL)");
            ArrayList<LabelItem> items = args.getParcelableArrayList(TAG_LABEL);
            FragmentManager fm = getFragmentManager();
            // adding item
            for (LabelItem item : items) {
                LabelItemFragment fragment = LabelItemFragment.newInstance(item);
                fragments.add(fragment);
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

        public void startAnimation() {
            ProgressBar pb = (ProgressBar)getView().findViewById(R.id.labelProgressBar);
            ObjectAnimator animation = ObjectAnimator.ofInt(pb,"progress",0, pb.getProgress());
            animation.setDuration(1000);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_label_item,container,false);


            final LabelItem label = getArguments().getParcelable(TAG_LABEL_ITEM);
            ((TextView)rootView.findViewById(R.id.labelDescription)).setText(label.description);

            int score = (int)(label.score*100);
            ((TextView)rootView.findViewById(R.id.labelScore)).setText(String.valueOf(score) + "%");

            ProgressBar pb = (ProgressBar)rootView.findViewById(R.id.labelProgressBar);
            pb.setProgress(score);

            return rootView;
        }

    }
}
