package com.aply.accountkeeper.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aply.accountkeeper.MainActivity;
import com.aply.accountkeeper.ProgressDialogFragment;
import com.aply.accountkeeper.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Adapter to hold transaction list to display in list view
 */
public class TransactionListAdapter extends BaseAdapter implements TransactionExchanger.OnSyncListener {

    private Context mContext;
    private ArrayList<MyTransaction> mTransList;
    private TransactionExchanger mExchanger;

    private Handler mHandler;

    private ProgressDialogFragment mProgressDialog;

    // TODO this is not a good way to decide if open fragment
    private boolean mIsPaused;

    public TransactionListAdapter(FragmentActivity activity) {
        mContext = activity;

        mTransList = new ArrayList<MyTransaction>();

        mHandler = new Handler(Looper.getMainLooper());

        mProgressDialog = null;

        mIsPaused = false;

        mExchanger = new TransactionExchanger(activity);
        mExchanger.start();
        mExchanger.syncBackend(MainActivity.GUID, null, this);
        if (null == mProgressDialog && false == mIsPaused) {
            mProgressDialog = new ProgressDialogFragment();
            mProgressDialog.show(activity.getSupportFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
        }
    }

    public void pause() {
        mIsPaused = true;
    }

    public void resume() {
        mIsPaused = false;
    }

    public void destroy() {
        if (null != mExchanger) {
            mExchanger.quit();
        }
    }

    public void refresh(final FragmentActivity activity, boolean isSyncBackend) {

        if (true == isSyncBackend) {
            mExchanger.syncBackend(MainActivity.GUID, null, this);
        }
        else {
            // TODO should really get actual update items??? or more good way to refresh??
            mExchanger.syncLocal(MainActivity.GUID, this);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (null == mProgressDialog && false == mIsPaused) {
                    mProgressDialog = new ProgressDialogFragment();
                    mProgressDialog.show(activity.getSupportFragmentManager(), ProgressDialogFragment.FRAGMENT_TAG);
                }
            }
        });
    }

    private void dismissDialog() {
        if (null != mProgressDialog) {
            if (true == mIsPaused) {
                mProgressDialog.setDismissOnResume();
            }
            else {
                mProgressDialog.dismiss();
            }
            mProgressDialog = null;
        }
    }

    @Override
    public void OnSyncError() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            }
        });
    }

    @Override
    public void OnSyncCompleted(final ArrayList<MyTransaction> newTransactionList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTransList.clear();
                mTransList.addAll(newTransactionList);
                notifyDataSetChanged();
                dismissDialog();
            }
        });
    }

    @Override
    public int getCount() {
        return mTransList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTransList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (0 > position || position >= mTransList.size()) {
            return convertView;
        }

        Holder holder = null;
        if (null != convertView) {
            holder = (Holder) convertView.getTag();
        }
        else {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.transaction_item, parent, false);
        }

        if (null == holder) {
            holder = new Holder((TextView) convertView.findViewById(R.id.trans_item_type),
                    (TextView) convertView.findViewById(R.id.trans_item_value),
                    (TextView) convertView.findViewById(R.id.trans_item_date));
            convertView.setTag(holder);
        }

        MyTransaction mt = mTransList.get(position);
        holder.mType.setText(mt.mKind);
        holder.mValue.setText(String.valueOf(mt.mValue));
        holder.mDate.setText(DateFormat.getDateTimeInstance().format(new Date(mt.mDate)) + " " + mt.mDate);

        return convertView;
    }

    public ArrayList<MyTransaction> getList() {
        return mTransList;
    }

    private static class Holder {
        TextView mType;
        TextView mValue;
        TextView mDate;
        public Holder(TextView type, TextView value, TextView date) {
            mType = type;
            mValue = value;
            mDate = date;
        }
    }
}
