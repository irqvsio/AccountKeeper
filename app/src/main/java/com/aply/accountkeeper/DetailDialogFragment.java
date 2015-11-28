package com.aply.accountkeeper;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.aply.accountkeeper.data.MyTransaction;

import java.util.ArrayList;

public class DetailDialogFragment extends DialogFragment {
    private static final String TAG = "DetailDialogFragment";

    // TODO maybe there is another good or managed tag name
    public static final String FRAGMENT_TAG = TAG;

    public static final String KEY_ALL_TRANSACTION = "key_all_transaction";

    public DetailDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO: should use backgroud thread to calculate
        ArrayList<Parcelable> list = getArguments().getParcelableArrayList(KEY_ALL_TRANSACTION);
        Double sum = 0d;
        for (Parcelable obj: list) {
            MyTransaction mt = (MyTransaction) obj;
            sum += mt.mValue;
        }

        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.detail_sum) + sum)
                .setPositiveButton(R.string.edit_ok, null)
                .create();
    }
}
