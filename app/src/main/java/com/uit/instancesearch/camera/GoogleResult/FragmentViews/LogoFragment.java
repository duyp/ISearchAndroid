package com.uit.instancesearch.camera.GoogleResult.FragmentViews;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uit.instancesearch.camera.GoogleResult.GoogleResultActivity;
import com.uit.instancesearch.camera.GoogleModels.LogoItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;

/**
 * Created by m on 21/01/2017.
 */

public class LogoFragment extends Fragment {

    static final String TAG_LOGO = "logo";
    public static final String TAG_INDEX = "index";

    public LogoFragment() {
    }

    public static LogoFragment newInstance(String queryImg, ArrayList<LogoItem> logoItems) {
        Bundle args = new Bundle();

        args.putString(GoogleResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelableArrayList(TAG_LOGO, logoItems);

        LogoFragment fragment = new LogoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_logo, container, false);

        Bundle args = getArguments();

        DrawableImageView imgView = (DrawableImageView) rootView.findViewById(R.id.logoImageView);
        String imgString = args.getString(GoogleResultActivity.TAG_QUERY_IMAGE_STRING);
        if (imgString!=null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));

        ArrayList<LogoItem> items = args.getParcelableArrayList(TAG_LOGO);
        for (LogoItem item : items) {
            imgView.addRectangle(item.points);
        }

        imgView.drawRectangles(DrawableImageView.COLOR_DEFAULT, DrawableImageView.RECT_DEFAULT_STROKE);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        int containerId = R.id.logoData;

        if (savedInstanceState == null) {
            int n = items.size();
            for (int i = 0; i < n; i++) {
                LogoItem item = items.get(i);
                ft.add(containerId, LogoItemFragment.newInstance(item,i));
            }
            ft.commit();
        }

        return rootView;
    }

    public static class LogoItemFragment extends Fragment {

        public static final String TAG_LOGO_ITEM = "logo_item";

        public LogoItemFragment() {

        }
        public static LogoItemFragment newInstance(LogoItem item, int index) {
            LogoItemFragment fragment = new LogoItemFragment();
            Bundle args = new Bundle();
            args.putParcelable(TAG_LOGO_ITEM,item);
            args.putInt(TAG_INDEX, index);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_logo_item,container,false);

            final LogoItem item = getArguments().getParcelable(TAG_LOGO_ITEM);
            final int index = getArguments().getInt(TAG_INDEX);

            // setting value
            int score = (int)(item.score*100);
            TextView scoreTextView = (TextView)rootView.findViewById(R.id.logoScore);
            scoreTextView.setText(String.valueOf(score) + "%");

            TextView descripTextView = (TextView)rootView.findViewById(R.id.logoDescription);
            descripTextView.setText(item.description);

            ProgressBar progressBar = (ProgressBar)rootView.findViewById(R.id.logoProgressBar);
            progressBar.setProgress(score);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GoogleResultActivity)getActivity())
                            .onHighlight(LogoFragment.class, index);
                }
            };

            descripTextView.setOnClickListener(onClickListener);
            scoreTextView.setOnClickListener(onClickListener);
            progressBar.setOnClickListener(onClickListener);

            return rootView;


        }
    }

}
