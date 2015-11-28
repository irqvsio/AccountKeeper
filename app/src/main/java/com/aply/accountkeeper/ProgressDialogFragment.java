package com.aply.accountkeeper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    private static final String TAG = "ProgressDialogFragment";

    public static final String FRAGMENT_TAG = TAG;

    private boolean mIsDismissOnResume;

    public ProgressDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsDismissOnResume = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (true == mIsDismissOnResume) {
            dismiss();
        }
    }

    public void setDismissOnResume() {
        mIsDismissOnResume = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage(getString(R.string.process_progressing));
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        return pd;
    }
}
