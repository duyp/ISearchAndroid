package com.uit.instancesearch.camera.GoogleResult;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.uit.instancesearch.camera.GoogleResult.FragmentViews.DrawableImageView;
import com.uit.instancesearch.camera.GoogleResult.FragmentViews.FaceFragment;
import com.uit.instancesearch.camera.GoogleResult.FragmentViews.LabelFragment;
import com.uit.instancesearch.camera.GoogleResult.FragmentViews.LandmarkFragment;
import com.uit.instancesearch.camera.GoogleResult.FragmentViews.LogoFragment;
import com.uit.instancesearch.camera.GoogleResult.FragmentViews.SafeSearchFragment;
import com.uit.instancesearch.camera.GoogleResult.FragmentViews.TextFragment;
import com.uit.instancesearch.camera.GoogleModels.FaceItem;
import com.uit.instancesearch.camera.GoogleModels.GoogleVisionResultData;
import com.uit.instancesearch.camera.GoogleModels.LogoItem;
import com.uit.instancesearch.camera.GoogleModels.SafeSearchItem;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.ui.dialog.MyCircleProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 16/01/2017.
 */

public class GoogleResultActivity extends AppCompatActivity
        implements GoogleResultView {

    public static final String TAG_QUERY_IMAGE_STRING = "query_image";
    public static final String TAG_RESULT_DATA = "result_data";

    public static final String TAG_LOGO_DATA = "logo_data";
    public static final String TAG_FACE_DATA = "face_data";
    public static final String TAG_SAFE_SEARCH_DATA = "safe_search_data";
    public static final String TAG_CURRENT_PAGE = "current_page";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    LandmarkFragment landmarkFragment;
    LabelFragment labelFragment;
    TextFragment textFragment;
    SafeSearchFragment safeSearchFragment;
    LogoFragment logoFragment;
    FaceFragment faceFragment;

    String queryImage;

    GoogleResultController controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_result);

        queryImage = getIntent().getStringExtra(TAG_QUERY_IMAGE_STRING);

        viewPager = (ViewPager) findViewById(R.id.gresult_viewpager);
        viewPager.setOffscreenPageLimit(5);

        tabLayout = (TabLayout) findViewById(R.id.gresult_tabs);

        controller = new GoogleResultControllerImpl(this);

        Bundle bundle = getIntent().getExtras();
        controller.setResultData((GoogleVisionResultData)bundle.getParcelable(TAG_RESULT_DATA));

        if (savedInstanceState != null) {
            // retrieval saved state
            ArrayList<FaceItem> faces = savedInstanceState.getParcelableArrayList(TAG_FACE_DATA);
            ArrayList<LogoItem> logos = savedInstanceState.getParcelableArrayList(TAG_LOGO_DATA);
            SafeSearchItem safesearch = savedInstanceState.getParcelable(TAG_SAFE_SEARCH_DATA);
            controller.addFaceData(faces);
            controller.addLogoData(logos);
            controller.addSafeSearchData(safesearch);

            // perform display data
            displayResultData();
            viewPager.setCurrentItem(savedInstanceState.getInt(TAG_CURRENT_PAGE));
        } else {
            // Execute addition query
            controller.requestAdditionData(queryImage);
            MyCircleProgressBar.show(this, "Getting addition data...");
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

        safeSearchFragment = SafeSearchFragment.newInstance(queryImage, data.getSafeSearch());
        adapter.addFragment(safeSearchFragment,
                            getString(R.string.safesearch_detection));

        logoFragment = LogoFragment.newInstance(queryImage,data.getLogos());
        adapter.addFragment(logoFragment, getString(R.string.logo_detection));

        faceFragment = FaceFragment.newInstance(queryImage, data.getFaces());
        adapter.addFragment(faceFragment, getString(R.string.face_detection));

        setupViewPageListener(vp);
        vp.setAdapter(adapter);

    }

    @Override
    public void displayResultData() {
        MyCircleProgressBar.dismiss();
        setupViewPager(viewPager, queryImage, controller.getResultData());
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPageListener(ViewPager vp) {
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: // landmark
                        landmarkFragment.startProgressBarAnimation();
                        break;
                    case 1: // label
                        labelFragment.startProgressBarAnimation();
                        break;
                    case 2: // text
                        break;
                    case 3: // safe search
                        safeSearchFragment.startProgressBarAnimation();
                        break;
                    case 4: // logo
                        break;
                    case 5: // face
                        break;
                    default: break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        controller.saveInstanceState(savedInstanceState);
        savedInstanceState.putInt(TAG_CURRENT_PAGE, viewPager.getCurrentItem());
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
}