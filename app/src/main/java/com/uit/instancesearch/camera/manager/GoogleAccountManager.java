package com.uit.instancesearch.camera.manager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.uit.instancesearch.camera.main.GoogleAccountListener;

import java.io.IOException;

/**id
 * Created by m on 15/01/2017.
 */

public class GoogleAccountManager {

    public static int REQUEST_PERMISSION_CODE = 54;

    Account account;
    Context context;
    AccountManager am;
    GetTokenTask getTokenTask;
    GoogleAccountListener listener;

    private String accessToken;

    public static String SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform";

    public GoogleAccountManager (Context c, GoogleAccountListener l, Account a) {
        account = a;
        context = c;
        listener = l;
    }

    public void getAuthToken() {
        getTokenTask = new GetTokenTask();
        getTokenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String getAccessToken() {
        return accessToken;
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
                listener.onTokenReceived();
            }
        }

        String fetchToken() {
            if (account == null) return null;
            try {
                String accessToken = GoogleAuthUtil.getToken(context,account,SCOPE);
                GoogleAuthUtil.clearToken(context,accessToken);
                return GoogleAuthUtil.getToken(context,account,SCOPE);
            } catch (UserRecoverableAuthException ue) {
                //mActivity.startActivityForResult(ue.getIntent(), CameraActivity.REQUEST_ACCOUNT_AUTHORIZATION);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
