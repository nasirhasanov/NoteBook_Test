package com.example.notebooktest.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.example.notebooktest.R;


public class ProgressDialog {

    public static ProgressDialog customProgress = null;
    private Dialog mDialog;

    public static ProgressDialog getInstance() {
        if (customProgress == null) {
            customProgress = new ProgressDialog();
        }
        return customProgress;
    }

    public void showProgress(Context context, String message) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_progress_new_note);
        TextView messageToUser = (TextView) mDialog.findViewById(R.id.message_to_user);
        messageToUser.setText(message);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
