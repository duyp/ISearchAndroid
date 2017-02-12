package com.uit.instancesearch.camera.UITResult;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.manager.MyCameraManager;
import com.uit.instancesearch.camera.manager.WSManager;

/**
 * Created by air on 2/12/17.
 */

public class UITResultViewImpl implements UITResultView, ResultListener
{
    ResultView 	resultView;

    FrameLayout queryView;
    ImageView queryImageView;
    //TextView			queryTextView;
    GridView resultGridView;
    ViewPager imagePager;
    ImagePagerAdapter	previewAdapter;

    LinearLayout resultMenu;
    ImageButton downloadButton;

    boolean viewing;
    boolean isPagerShown = false;

    UITResultController controller;

    Activity parentActivity;

    public UITResultViewImpl(Activity parent) {
        controller = new UITResultControllerImpl(this);
        initViews(parent);
        parentActivity = parent;
    }

    @Override
    public void initViews(Activity parent) {
        queryImageView = new ImageView(parent);
        queryView = (FrameLayout)parent.findViewById(R.id.image_view);

        resultView = (ResultView)parent.findViewById(R.id.result_view);

        resultView.initManager(controller.getResultManager());
        resultGridView = resultView.getGridView();

        imagePager = new ViewPager(parent);
        previewAdapter = new ImagePagerAdapter(parent, controller.getResultManager());
        imagePager.setAdapter(previewAdapter);

        resultMenu = (LinearLayout)parent.findViewById(R.id.result_menu);
        downloadButton = (ImageButton)parent.findViewById(R.id.save_button);

        viewing = false;

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            int pressedX = 0;
            boolean pressed = false;
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    pressed = true;
                    pressedX = (int)e.getRawX();
                    onResultViewPrepareResize();
                } else if (e.getAction() == MotionEvent.ACTION_MOVE){
                    if (pressed) {
                        int dx = pressedX - (int)e.getRawX();
                        //Log.d("debug", "x: " + e.getRawX());
                        onResultViewResize(dx);
                    }
                } else if (e.getAction() == MotionEvent.ACTION_UP) {
                    pressed = false;
                    onResultViewPrepareResize();
                }
                return true;
            }
        };


        resultView.setOnTouchListener(touchListener);
        queryImageView.setOnTouchListener(touchListener);

        resultGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //if (itemClickEnabled) {
                itemClick(position);
                //}
            }
        });

        imagePager.setOffscreenPageLimit(2);
        imagePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (isPagerShown)
                    setPreview(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                controller.downloadImage(imagePager.getCurrentItem());
            }
        });
    }


    @Override
    public void invalidateResult() {
        resultView.invalidateResult();
        previewAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPreview(int position) {
        controller.requestPreviewImage(position);
    }

    @Override
    public void showResultMenu() {
        resultMenu.animate().translationY(0).setDuration(1000).
                setInterpolator(new OvershootInterpolator()).start();
    }

    @Override
    public void hideResultMenu() {
        resultMenu.animate().translationY(resultMenu.getHeight()).setDuration(1000).
                setInterpolator(new OvershootInterpolator()).start();
    }

    @Override
    public boolean isShown() {
        return viewing;
    }

    @Override
    public void clearResults() {
        queryView.removeAllViews();
        resultView.invalidateResult();
    }

    @Override
    public void setResultRankedList(String[] rankedList) {
        controller.setResultRankedList(rankedList);
    }

    @Override
    public void setResultImage(String tag, String imgId, Bitmap result) {
        controller.setResultImage(tag,imgId,result);
    }

    @Override
    public void showResultView() {
        // result view
        ViewGroup.LayoutParams p = resultView.getLayoutParams();
        p.width = MyCameraManager.getScreenSize().x*2/5;
        resultView.setLayoutParams(p);
        resultView.restart();
        resultView.show();

        // query view
        ViewGroup.LayoutParams p1 = queryView.getLayoutParams();
        p1.width = MyCameraManager.getScreenSize().x - p.width;
        queryView.setLayoutParams(p1);

        //API 14
        queryView.animate().translationX(0).setDuration(1000).
                setInterpolator(new OvershootInterpolator()).start();
        viewing = true;

        ViewGroup.LayoutParams p2 = resultMenu.getLayoutParams();
        p2.width = p1.width;
        resultMenu.setLayoutParams(p2);

        showQueryImageView();
    }

    @Override
    public void hideResultView() {
        resultView.hide();
        hideResultMenu();
        queryView.animate().translationX(-queryView.getWidth()).setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();
        viewing = false;
        //imagePager.setCurrentItem(0);
    }

    private void showQueryImageView() {
        isPagerShown = false;
//        queryView.removeView(imagePager);
        queryView.removeAllViews();
        queryView.addView(queryImageView);
    }

    private void showImagePager() {
        isPagerShown = true;
//        queryView.removeView(queryImageView);
        queryView.removeAllViews();
        queryView.addView(imagePager);
    }

    @Override
    public void setQueryImage(Bitmap bm) {
        queryImageView.setImageBitmap(bm);
    }


    private void itemClick(int position) {
        ResultManager manager = controller.getResultManager();
        if (position < manager.getThumbnailCount()) { // click for preview image
            if (!isPagerShown) {
                showImagePager();
                showResultMenu();
            }
            imagePager.setCurrentItem(position, false);
        } else if (position == manager.getThumbnailCount() + manager.getLoadingThumbCount()) { // click for get more image
            controller.getMoreImage();
            resultView.invalidateResult();
        } else {

        }
    }

    // Result Listener
    @Override
    public void onResultViewPrepareResize() {
        resultView.prepareResize();
    }

    @Override
    public void onResultViewResized() {
        resultView.prepareResize();
    }

    @Override
    public void onResultViewResize(int dx) {
        //queryTextView.setText(String.valueOf(dx));
        int x;
        if ((x=resultView.resize(dx)) != 0) {
            ViewGroup.LayoutParams p1 = queryView.getLayoutParams();
            p1.width = x;
            queryView.setLayoutParams(p1);
            //queryView.invalidate();

            ViewGroup.LayoutParams p2 = resultMenu.getLayoutParams();
            p2.width = p1.width;
            resultMenu.setLayoutParams(p2);
            //resultMenu.invalidate();
        }
    }

    @Override
    public Context getContext() {
        return parentActivity;
    }
}
