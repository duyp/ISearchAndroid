package com.uit.instancesearch.camera.main.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ListView;

import com.uit.instancesearch.camera.ProcessingServer.UITImageRetrievalServer;
import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.manager.WSManager;

/**
 * Created by air on 2/13/17.
 */

public class ServerIPDialogFragment extends  DialogFragment {
    public interface ServerIPDialogListener {
        void onServerIPDialogPositiveClick(String serverIP);
    }

    ServerIPDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final EditText editText = new EditText(this.getActivity());
        editText.setText(UITImageRetrievalServer.DEFAULT_IP);
        builder.setTitle("Plz tell me your server IP address !")
                .setView(editText)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onPositiveClick(editText.getText().toString());
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(1);
                    }
                })
                .setCancelable(false);
        return builder.create();
    }

    void onPositiveClick(String text) {
        mListener.onServerIPDialogPositiveClick(text);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ServerIPDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
