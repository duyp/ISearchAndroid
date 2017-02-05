package com.uit.instancesearch.camera.ui.GoogleVisionResult;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.FaceItem;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.GoogleVisionResultData;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.LogoItem;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationObject.SafeSearchItem;
import com.uit.instancesearch.camera.ProcessingServer.GoogleImageAnnotationPostServer;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.listener.GoogleCloudVisionRequestListener;
import com.uit.instancesearch.camera.tools.ImageTools;
import com.uit.instancesearch.camera.ui.dialog.MyCircleProgressBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 16/01/2017.
 */

public class GoogleVisionResultActivity extends AppCompatActivity
                                        implements GoogleCloudVisionRequestListener {

    public static final String TAG_QUERY_IMAGE_STRING = "query_image";
    public static final String TAG_RESULT_DATA = "result_data";

    private static final String TAG_LOGO_DATA = "logo_data";
    private static final String TAG_FACE_DATA = "face_data";
    private static final String TAG_SAFE_SEARCH_DATA = "safe_search_data";
    private static final String TAG_CURRENT_PAGE = "current_page";


    private TabLayout tabLayout;
    private ViewPager viewPager;

    LandmarkFragment landmarkFragment;
    LabelFragment labelFragment;
    TextFragment textFragment;
    LogoFragment logoFragment;
    FaceFragment faceFragment;

    GoogleImageAnnotationPostServer server;
    GoogleVisionResultData resultData;
    String queryImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_result);

        queryImage = getIntent().getStringExtra(TAG_QUERY_IMAGE_STRING);

        viewPager = (ViewPager) findViewById(R.id.gresult_viewpager);
        viewPager.setOffscreenPageLimit(5);

        tabLayout = (TabLayout) findViewById(R.id.gresult_tabs);

        Bundle bundle = getIntent().getExtras();
        resultData = bundle.getParcelable(TAG_RESULT_DATA);

        if (savedInstanceState != null) {
            // retrieval saved state
            ArrayList<FaceItem> faces = savedInstanceState.getParcelableArrayList(TAG_FACE_DATA);
            ArrayList<LogoItem> logos = savedInstanceState.getParcelableArrayList(TAG_LOGO_DATA);
            SafeSearchItem safesearchs = savedInstanceState.getParcelable(TAG_SAFE_SEARCH_DATA);
            resultData.setFaces(faces);
            resultData.setLogos(logos);
            resultData.setSafeSearch(safesearchs);

            // perform display data
            setupViewPager(viewPager, queryImage, resultData);
            tabLayout.setupWithViewPager(viewPager);

            viewPager.setCurrentItem(savedInstanceState.getInt(TAG_CURRENT_PAGE));
        } else {
            // Execute addition query
            server = new GoogleImageAnnotationPostServer(this);
            MyCircleProgressBar.show(this, "Getting addition data...");
            server.executeQueryRequest(ImageTools.decodeStringToBitmap(queryImage));
        }
    }

    private void setupViewPager(ViewPager vp, String queryImage, GoogleVisionResultData data) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        landmarkFragment = LandmarkFragment.newInstance(queryImage, data.getLandmark());
        adapter.addFragment(landmarkFragment, getString(R.string.landmark_detection));

        labelFragment = LabelFragment.newInstance(queryImage, data.getLabels());
        adapter.addFragment(labelFragment, getString(R.string.label_detection));

        textFragment = TextFragment.newInstance(queryImage, data.getTexts());
        adapter.addFragment(textFragment, getString(R.string.text_detection));


        adapter.addFragment(SafeSearchFragment.newInstance(queryImage, data.getSafeSearch()),
                            getString(R.string.safesearch_detection));

        logoFragment = LogoFragment.newInstance(queryImage,data.getLogos());
        adapter.addFragment(logoFragment, getString(R.string.logo_detection));

        faceFragment = FaceFragment.newInstance(queryImage, data.getFaces());
        adapter.addFragment(faceFragment, getString(R.string.face_detection));

        vp.setAdapter(adapter);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(TAG_FACE_DATA, resultData.getFaces());
        savedInstanceState.putParcelableArrayList(TAG_LOGO_DATA, resultData.getLogos());
        savedInstanceState.putParcelable(TAG_SAFE_SEARCH_DATA, resultData.getSafeSearch());
        savedInstanceState.putInt(TAG_CURRENT_PAGE, viewPager.getCurrentItem());
    }

    @Override
    public void onCloudVisionRespond(JSONObject response) {
        if (response != null) {
            resultData.setLogos(GoogleVisionResultData.getLogoItem(response));
            resultData.setSafeSearch(GoogleVisionResultData.getSafeSearchItem(response));
            resultData.setFaces(GoogleVisionResultData.getFaceItem(response));
        } else {
            Toast.makeText(this,"Cannot get addition data !", Toast.LENGTH_SHORT);
        }
        MyCircleProgressBar.dismiss();
        setupViewPager(viewPager, queryImage, resultData);
        tabLayout.setupWithViewPager(viewPager);
    }

    // open google maps application with specific lat, long
    public static void onLandmarkClickOpenGoogleMap(Context c, double latitude, double longitude,
                                                    String locationName) {
        Uri ggmItentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + locationName);
        Intent intent = new Intent(Intent.ACTION_VIEW, ggmItentUri);
        intent.setPackage("com.google.android.apps.maps");
        c.startActivity(intent);
    }

    public void onHighlight(Class T, int index) {
        Log.d("TAG_HIGHLIGHT", "highlight OK !");
        if (T == LandmarkFragment.class) {
            DrawableImageView imgView = (DrawableImageView) findViewById(R.id.landmarkImageView);
            if (imgView!= null) imgView.setHighlightIndex(index);
        } else if (T == TextFragment.class) {
            //textFragment.highlightIndex(index);
            DrawableImageView imgView = (DrawableImageView) findViewById(R.id.textImageView);
            if (imgView!= null) imgView.setHighlightIndex(index);
        } else if (T == LogoFragment.class) {
            DrawableImageView imgView = (DrawableImageView) findViewById(R.id.logoImageView);
            if (imgView!= null) imgView.setHighlightIndex(index);
        }
    }

}