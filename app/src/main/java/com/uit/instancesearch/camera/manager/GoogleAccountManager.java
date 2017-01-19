package com.uit.instancesearch.camera.manager;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.uit.instancesearch.camera.ui.CameraActivity;

import java.io.IOException;

/**id
 * Created by m on 15/01/2017.
 */

public class GoogleAccountManager {

    public static int REQUEST_PERMISSION_CODE = 54;

    Account account;
    Activity mActivity;
    AccountManager am;
    GetTokenTask getTokenTask;

    private String accessToken;

    public static String SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform";

    public GoogleAccountManager (Account a, Activity parent) {
        account = a;
        mActivity = parent;
    }

    public void getAuthToken() {
        getTokenTask = new GetTokenTask();
        getTokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String getAccessToken() {
        return accessToken;
    }

    // MUST Implements for Android 6 (v23)
    public void requestAccountPermission(CameraActivity activity) {
        ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.GET_ACCOUNTS}, REQUEST_PERMISSION_CODE);
    }

    public void cancelExecute() {
        if (getTokenTask!= null && !getTokenTask.isCancelled()) getTokenTask.cancel(true);
    }

    private class GetTokenTask extends AsyncTask<Void, String, String> {

        String token;

        @Override
        protected String doInBackground(Void... paramas) {
            return fetchToken();
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isCancelled()) {
                accessToken = result;
                ((CameraActivity)mActivity).onTokenReceived();
            }
        }

        String fetchToken() {
            if (account == null) return null;
            try {
                String accessToken = GoogleAuthUtil.getToken(mActivity,account,SCOPE);
                GoogleAuthUtil.clearToken(mActivity,accessToken);
                return GoogleAuthUtil.getToken(mActivity,account,SCOPE);
            } catch (UserRecoverableAuthException ue) {
                mActivity.startActivityForResult(ue.getIntent(), CameraActivity.REQUEST_ACCOUNT_AUTHORIZATION);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
