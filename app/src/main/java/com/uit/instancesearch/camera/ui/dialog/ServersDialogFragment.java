package com.uit.instancesearch.camera.ui.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.uit.instancesearch.camera.R;
import com.uit.instancesearch.camera.manager.WSManager;

/**
 * Created by m on 14/01/2017.
 */

public class ServersDialogFragment extends DialogFragment {

    public interface ServersDialogListener {
        public void onServersDialogPositiveClick(DialogFragment dialog, int chosenId);
    }

    ServersDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose sever")
                .setSingleChoiceItems(R.array.server_list,0, null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ListView l = ((AlertDialog)dialogInterface).getListView();
                        int serverId = l.getCheckedItemPosition();
                        Log.w("selected: " + serverId,"dialog");
                        mListener.onServersDialogPositiveClick(ServersDialogFragment.this, serverId==0? WSManager.SERVER_UIT:WSManager.SERVER_GOOGLE);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ServersDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
