package com.uit.instancesearch.camera;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;

import com.uit.instancesearch.camera.tools.ImageTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m on 16/01/2017.
 */

public class GoogleVisionResultActivity extends AppCompatActivity {

    public static String TAG_QUERY_IMAGE_STRING = "query_image";
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_result);


        String queryImage = getIntent().getStringExtra(TAG_QUERY_IMAGE_STRING);
        viewPager = (ViewPager)findViewById(R.id.gresult_viewpager);
        setupViewPager(viewPager,queryImage);

        tabLayout = (TabLayout)findViewById(R.id.gresult_tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager vp, String queryImage) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(ResultFragment.newInstance(queryImage),getString(R.string.landmark_detection));
        adapter.addFragment(ResultFragment.newInstance(queryImage),getString(R.string.label_detection));
        adapter.addFragment(ResultFragment.newInstance(queryImage),getString(R.string.text_detection));
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

    public static class ResultFragment extends Fragment {

       public ResultFragment() {

       }

        public static ResultFragment newInstance(String queryImage) {
            Bundle args = new Bundle();
            args.putString(TAG_QUERY_IMAGE_STRING,queryImage);
            ResultFragment fragment = new ResultFragment();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.result_fragment,container,false);
            ImageView imgView = (ImageView)rootView.findViewById(R.id.gresult_imageView);
            String imgString = getArguments().getString(TAG_QUERY_IMAGE_STRING);
            imgView.setImageBitmap(ImageTools.decodeStringToBitmap(imgString));
            return rootView;
        }
    }
}
