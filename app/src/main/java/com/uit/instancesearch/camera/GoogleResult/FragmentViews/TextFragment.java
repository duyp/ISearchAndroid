package com.uit.instancesearch.camera.GoogleResult.FragmentViews;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uit.instancesearch.camera.GoogleResult.GoogleResultActivity;
import com.uit.instancesearch.camera.GoogleModels.TextItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;

/**
 * Created by m on 21/01/2017.
 */

public class TextFragment extends Fragment {

    static final String TAG_TEXT = "text";

    public TextFragment() {

    }

    public static TextFragment newInstance(String queryImg, ArrayList<TextItem> texts) {
        Bundle args = new Bundle();
        Log.d("TAG_DEBUG", "found " + texts.size() + " texts");

        args.putString(GoogleResultActivity.TAG_QUERY_IMAGE_STRING, queryImg);
        args.putParcelableArrayList(TAG_TEXT, texts);

        TextFragment fragment = new TextFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_text, container, false);

        final DrawableImageView imgView = (DrawableImageView) rootView.findViewById(R.id.textImageView);
        Bundle args = getArguments();

        String imgString = args.getString(GoogleResultActivity.TAG_QUERY_IMAGE_STRING);
        if (imgString != null) imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));


        final ArrayList<TextItem> items = args.getParcelableArrayList(TAG_TEXT);

        // draw text position on image
        for(TextItem i : items) {
            imgView.addRectangle(i.vertices);
        }

        imgView.drawRectangles(DrawableImageView.COLOR_DEFAULT, DrawableImageView.RECT_MIN_STROKE);

        // adding fragments
        if (savedInstanceState == null) {
            Log.d("TAG_DEBUG","received " + items.size() + " texts");
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (items != null) {
                int n = items.size();
                for (int i = 0; i < n; i++) {
                    TextItem item = items.get(i);
                    Fragment f = TextItemFragment.newInstance(item, i);
                    if (!f.isAdded()) ft.add(R.id.textData,f);
                }
                ft.commit();
            }
        }

        return rootView;
    }

    public static class TextItemFragment extends Fragment {

        public static final String TAG_TEXT_ITEM = "landmark_item";
        public static final String TAG_INDEX = "index";

        public TextItemFragment() {

        }

        public static TextItemFragment newInstance(TextItem item, int itemIndex) {
            TextItemFragment fragment = new TextItemFragment();
            Bundle args = new Bundle();
            args.putParcelable(TAG_TEXT_ITEM, item);
            args.putInt(TAG_INDEX, itemIndex);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_text_item, container, false);

            final TextItem item = getArguments().getParcelable(TAG_TEXT_ITEM);
            final int index = getArguments().getInt(TAG_INDEX);
            // setting value
            TextView textView = (TextView) rootView.findViewById(R.id.textDescription);
            textView.setText(item.content);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GoogleResultActivity)getActivity()).onHighlight(TextFragment.class, index);
                }
            });


            return rootView;
        }

    }
}
