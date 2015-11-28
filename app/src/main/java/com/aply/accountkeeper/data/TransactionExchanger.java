package com.aply.accountkeeper.data;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.aply.accountkeeper.Constants;
import com.aply.accountkeeper.MainActivity;
import com.bean_keeper.Proto.Transaction;
import com.bean_keeper.Proto.AccountDelta;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import okio.BufferedSource;

/**
 * Data loader to load data from local storage and sync data from backend
 * server.
 */
public class TransactionExchanger extends HandlerThread {
    private static final String TAG = "TransactionExchanger";

    /**
     * When syncing with backend server or loading local data, we will notify
     * the status via this listener.
     */
    public interface OnSyncListener {
        int TYPE_SYNC_BACKEND = 1;
        int TYPE_SYNC_LOCAL = 2;

        /**
         * Called if sync fail for any reason.
         */
        void OnSyncError();
        /**
         * Called if sync successfully
         * @param type the purpose of this sync task
         * @param newTransactionList the result of transaction list
         */
        void OnSyncCompleted(int type, ArrayList<MyTransaction> newTransactionList);
    }

    public final MediaType APPLICATION_OCTET_STREAM = MediaType.parse("application/octet-stream");
    private static final String BACKEND_ADDRESS = "http://homework.avast.ninja/bk";

    private static final int MSG_ID_SYNC_BACKEND = 1;
    private static final int MSG_ID_SYNC_LOCAL = 2;

    private Context mContext;
    private Handler mHandler;

    public TransactionExchanger(Context cxt) {
        super(TAG);
        mContext = cxt;
        mHandler = null;
    }

    private void ensureLooperPrepared() {
        if (null == mHandler) {
            mHandler = new MyHandler(getLooper());
        }
    }

    public void syncLocal(String guid, OnSyncListener listener) {
        ensureLooperPrepared();
        ArgumentObj arg = new ArgumentObj();
        arg.guid = guid;
        arg.listener = listener;

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_ID_SYNC_LOCAL;
        msg.obj = arg;
        mHandler.sendMessage(msg);
    }

    private void syncLocalInternal(ArgumentObj arg) {
        final OnSyncListener listener = arg.listener;

        if (null != listener) {
            DataStore dataStore = MainActivity.getDataStore(mContext);
            // Get all un-deleted transactions from DataStore
            ArrayList<MyTransaction> result = dataStore.query(DataStore.COL_NAME_DEL + "=?", new String[]{String.valueOf(0)});
            if (Constants.DEBUG) dump(result);
            listener.OnSyncCompleted(OnSyncListener.TYPE_SYNC_LOCAL, result);
        }
    }

    public void syncBackend(String guid, ArrayList<MyTransaction> list, OnSyncListener listener) {
        ensureLooperPrepared();

        if (null == list) {
            list = new ArrayList<MyTransaction>();
        }
        ArgumentObj arg = new ArgumentObj();
        arg.guid = guid;
        arg.syncList = list;
        arg.listener = listener;

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_ID_SYNC_BACKEND;
        msg.obj = arg;
        mHandler.sendMessage(msg);
    }

    private void syncBackendInternal(ArgumentObj arg) {
        final DataStore dataStore = MainActivity.getDataStore(mContext);

        //final int id = arg.id;
        final String guid = arg.guid;
        final OnSyncListener listener = arg.listener;
        final ArrayList<MyTransaction> syncList;
        if (0 < arg.syncList.size()) {
            syncList = arg.syncList;
        }
        else {
            syncList = dataStore.query(DataStore.COL_NAME_SYNC + "=?", new String[]{String.valueOf(0)});
        }

        // make sure all are un-sync items
        TreeMap<Long, MyTransaction> updateTransMap = new TreeMap<Long, MyTransaction>();
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        for (MyTransaction mt : syncList) {
            if (null == mt) continue;

            if (false == mt.mIsSynced) {
                mt.mIsSynced = true;
                updateTransMap.put(mt.mDate, mt);
                list.add(mt.Build());
            }
        }

        long lastSyncTime = dataStore.getSyncTime();

        if (true == Constants.DEBUG) Log.d(TAG, "last serverTimestamp " + lastSyncTime + " " + list.size());
        AccountDelta requestAccDelta = AccountDelta.newBuilder()
                .setServerTimestamp(lastSyncTime)
                .addAllAddedOrModified(list)
                .build();
        byte[] bytes = requestAccDelta.toByteArray();

        RequestBody requestBody = RequestBody.create(APPLICATION_OCTET_STREAM, bytes);

        Response response = null;
        try {
            OkHttpClient httpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(BACKEND_ADDRESS)
                    .post(requestBody)
                    .build();

            response = httpClient.newCall(request).execute();
        }
        catch (IOException e) {
            if (true == Constants.DEBUG) Log.d(TAG, "Can't connect with backend");
            response = null;
        }

        if (null == response) {
            if (null != listener) {
                listener.OnSyncError();
            }
            return;
        }

        AccountDelta responseAccDelta = null;
        try {
            BufferedSource source = response.body().source();
            responseAccDelta = AccountDelta.parseFrom(source.readByteArray());
            source.close();
        }
        catch (IOException e) {
            if (true == Constants.DEBUG) Log.d(TAG, "Can't sync from backend");
            responseAccDelta = null;
        }

        if (null == responseAccDelta) {
            if (null != listener) {
                listener.OnSyncError();
            }
            return;
        }

        long time = responseAccDelta.getServerTimestamp();

        dataStore.setSyncTime(time);
        if (true == Constants.DEBUG) Log.d(TAG, "new serverTimestamp " + time + ", list size=" + responseAccDelta.getAddedOrModifiedList().size());

        TreeMap<Long, MyTransaction> insertTransMap = new TreeMap<Long, MyTransaction>();
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<String>();
        for (Transaction t : responseAccDelta.getAddedOrModifiedList()) {
            if(null == t || null == t.getGuid()) {
                continue;
            }

            if(true == t.getGuid().contains(guid)) {
                MyTransaction mt = new MyTransaction(t);
                mt.mIsSynced = true;

                if (true == updateTransMap.containsKey(mt.mDate)) {
                    updateTransMap.put(mt.mDate, mt);
                }
                else {
                    insertTransMap.put(mt.mDate, mt);
                    selectionArgs.add(String.valueOf(mt.mDate));
                    if (0 >= selection.length()) {
                        selection.append(DataStore.COL_NAME_DATE + "=?");
                    }
                    else {
                        selection.append(" OR " + DataStore.COL_NAME_DATE + "=?");
                    }
                }
            }
        }

        if (0 < selectionArgs.size()) {
            ArrayList<MyTransaction> tmpList = dataStore.query(selection.toString(), selectionArgs.toArray(new String[1]));
            for (MyTransaction mt: tmpList) {
                if (true == insertTransMap.containsKey(mt.mDate)) {
                    MyTransaction newMT = insertTransMap.remove(mt.mDate);
                    updateTransMap.put(newMT.mDate, newMT);
                }
            }
        }

        // disable notification temporarily due to we will sync all after modifying db
        dataStore.setEnableDataChangeObserver(false);
        dataStore.bucketUpdate(updateTransMap.values());
        dataStore.bucketInsert(insertTransMap.values());
        dataStore.setEnableDataChangeObserver(true);

        if (null != listener) {
            // Get all un-deleted transactions from DataStore
            ArrayList<MyTransaction> result = dataStore.query(DataStore.COL_NAME_DEL + "=?", new String[]{String.valueOf(0)});
            if (Constants.DEBUG) dump(result);
            listener.OnSyncCompleted(OnSyncListener.TYPE_SYNC_BACKEND, result);
        }
    }

    private void dump(ArrayList<MyTransaction> mtList) {
        if (false == Constants.DEBUG) return;

        for (MyTransaction mt : mtList) {
            if (true == mt.mDeleted) {
                Log.d(TAG, "deleted " + mt.toString());
            }
            else {
                Log.d(TAG, "keep " + mt.toString());
            }
        }
    }

    private class ArgumentObj {
        //int id;
        String guid;
        ArrayList<MyTransaction> syncList;
        OnSyncListener listener;

    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_ID_SYNC_BACKEND:
                {
                    syncBackendInternal((ArgumentObj) msg.obj);
                    break;
                }
                case MSG_ID_SYNC_LOCAL:
                {
                    syncLocalInternal((ArgumentObj) msg.obj);
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
    }
}
