package com.aply.accountkeeper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aply.accountkeeper.data.MyTransaction;


/**
 * Created by APLY on 2015/11/1.
 */
public class EditTransactionFragment extends DialogFragment {
    private static final String TAG = "EditTransactionFragment";

    // TODO maybe there is another good or managed tag name
    public static final String FRAGMENT_TAG = TAG;

    public static final String KEY_EDIT_TYPE = "key_edit_type";
    public static final int EDIT_TYPE_ADD = 1;
    public static final int EDIT_TYPE_EDIT = 2;

    public static final String KEY_EDIT_TRANSACTION = "key_edit_transaction";

    private TextView mTypeView;
    private TextView mValueView;

    public static EditTransactionFragment getInstance(int type, MyTransaction transaction) {
        Bundle arg = new Bundle();
        EditTransactionFragment fragment = new EditTransactionFragment();

        switch (type) {
            case EDIT_TYPE_ADD:
            {
                arg.putInt(KEY_EDIT_TYPE, EDIT_TYPE_ADD);
                fragment.setArguments(arg);
                break;
            }
            case EDIT_TYPE_EDIT:
            {
                arg.putInt(KEY_EDIT_TYPE, EDIT_TYPE_EDIT);
                arg.putParcelable(KEY_EDIT_TRANSACTION, transaction);
                fragment.setArguments(arg);
                break;
            }
            default:
            {
                break;
            }
        }

        return fragment;
    }

    public EditTransactionFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_edit_trans, null, false);

        mTypeView = (TextView) view.findViewById(R.id.item_edit_type);
        mValueView = (TextView) view.findViewById(R.id.item_edit_value);

        int type = getArguments().getInt(KEY_EDIT_TYPE);
        int title = R.string.transaction_edit_title;
        switch (type) {
            case EDIT_TYPE_ADD:
            {
                title = R.string.transaction_edit_title_new;
                break;
            }
            case EDIT_TYPE_EDIT:
            {
                MyTransaction mt = (MyTransaction) getArguments().get(KEY_EDIT_TRANSACTION);
                title = R.string.transaction_edit_title_edit;
                mTypeView.setText(mt.mKind);
                mValueView.setText(String.valueOf(mt.mValue));
                break;
            }
            default:
            {
                break;
            }
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.edit_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    CharSequence type = mTypeView.getText();
                                    CharSequence value = mValueView.getText();

                                    MyTransaction mt = null;
                                    switch (getArguments().getInt(KEY_EDIT_TYPE)) {
                                        case EDIT_TYPE_ADD:
                                            mt = new MyTransaction(MainActivity.GUID,
                                                    Double.valueOf(value.toString()),
                                                    type.toString());
                                            MainActivity.getDataStore(getActivity()).insert(mt);
                                            break;
                                        case EDIT_TYPE_EDIT:
                                            mt = (MyTransaction) getArguments().get(KEY_EDIT_TRANSACTION);
                                            mt.mKind = type.toString();
                                            mt.mValue = Double.valueOf(value.toString());
                                            mt.mIsSynced = false;
                                            MainActivity.getDataStore(getActivity()).update(mt);
                                            break;
                                    }
                                } catch (Exception e) {
                                    int type = getArguments().getInt(KEY_EDIT_TYPE);
                                    int errorString = R.string.edit_add_fail_error;
                                    if (type == EDIT_TYPE_EDIT) {
                                        errorString = R.string.edit_update_fail_error;
                                    }
                                    Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                )
                .setNegativeButton(R.string.edit_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                )
                .create();
    }
}
