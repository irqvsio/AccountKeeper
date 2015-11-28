package com.aply.accountkeeper.data;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

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

import okio.BufferedSource;

/**
 * Data loader to load data from local storage and sync data from backend
 * server.
 */
// TODO need another mechanism to sync data efficiently. Current sync algorithm is bad.
public class TransactionExchanger extends HandlerThread {
    private static final String TAG = "TransactionExchanger";

    /**
     * When syncing with backend server or loading local data, we will notify
     * the status via this listener.
     */
    // TODO add unique id to distinguish each sync task
    public interface OnSyncListener {
        /**
         * Called if sync fail for any reason.
         */
        void OnSyncError();

        /**
         * Called if sync successfully
         * @param newTransactionList the result of transaction list
         */
        void OnSyncCompleted(ArrayList<MyTransaction> newTransactionList);
    }

    public final MediaType APPLICATION_OCTET_STREAM = MediaType.parse("application/octet-stream");
    private static final String BACKEND_ADDRESS = "http://homework.avast.ninja/bk";

    private static final int MSG_ID_SYNC_BACKEND = 1;
    private static final int MSG_ID_SYNC_LOCAL = 2;

    private Context mContext;
    private Handler mHandler;
    //private AtomicInteger mSyncIdGenerter;

    public TransactionExchanger(Context cxt) {
        super(TAG);
        mContext = cxt;
        mHandler = null;
        //mSyncIdGenerter = new AtomicInteger(1);
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
//        final String guid = arg.guid;
        final OnSyncListener listener = arg.listener;

        if (null != listener) {
            DataStore dataStore = MainActivity.getDataStore(mContext);
            // Get all transactions from DataStore
            listener.OnSyncCompleted(filterDeleted(dataStore.query(null, null)));
        }
    }

    // TODO should be more better design to sync data, rather than always search from list
    public void syncBackend(String guid, ArrayList<MyTransaction> list, OnSyncListener listener) {
        ensureLooperPrepared();

        if (null == list) {
            list = new ArrayList<MyTransaction>();
        }
        ArgumentObj arg = new ArgumentObj();
        //arg.id = mSyncIdGenerter.getAndIncrement();
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
            syncList = dataStore.query(null, null);
        }

        // find all un-sync items
        ArrayList<MyTransaction> remove = new ArrayList<MyTransaction>();
        for (MyTransaction mt : syncList) {
            if (null != mt && true == mt.mIsSynced) {
                remove.add(mt);
            }
        }
        syncList.removeAll(remove);

        long lastSyncTime = dataStore.getSyncTime();
        // find all un-sync items
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        for (MyTransaction mt : syncList) {
            if (null != mt) {
                list.add(mt.Build());
            }
        }
        Log.d(TAG, "last serverTimestamp " + lastSyncTime + " " + list.size());
        AccountDelta requestAccDelta = AccountDelta.newBuilder()
                .setServerTimestamp(lastSyncTime)
                .addAllAddedOrModified(list)
                .build();
        byte[] bytes = requestAccDelta.toByteArray();

        RequestBody requestBody = RequestBody.create(APPLICATION_OCTET_STREAM, bytes);

//        RequestBody requestBody = new RequestBody() {
//            @Override
//            public MediaType contentType() {
//                return MEDIA_TYPE_MARKDOWN;
//            }
//
//            @Override
//            public void writeTo(BufferedSink sink) throws IOException {
//
//                long time = dataStore.getSyncTime();
//
//                // find all un-sync items
//                ArrayList<Transaction> list = new ArrayList<Transaction>();
//                for (MyTransaction mt : syncList) {
//                    if (null != mt) {
//                        list.add(mt.Build());
//                    }
//                }
//                Log.d(TAG, "last serverTimestamp " + time + " " + list.size());
//                AccountDelta requestAccDelta = new AccountDelta.Builder()
//                        .serverTimestamp(time)
//                        .addedOrModified(list)
//                        .build();
//
//                ProtoWriter writer = new ProtoWriter(sink);
//                AccountDelta.ADAPTER.encode().encode(writer, requestAccDelta);
//            }
//        };

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
            Log.d(TAG, "Can't connect with backend");
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
            Log.d(TAG, "Can't sync from backend");
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
        Log.d(TAG, "new serverTimestamp " + time + ", list size=" + responseAccDelta.getAddedOrModifiedList().size());

        ArrayList<MyTransaction> transList = new ArrayList<MyTransaction>();
        for (Transaction t : responseAccDelta.getAddedOrModifiedList()) {
            if (null == t || null == t.getGuid()) {
                continue;
            }

            if(true == t.getGuid().contains(guid)) {
                MyTransaction mt = new MyTransaction(t);
                mt.mIsSynced = true;

                // TODO need refiine the match algorithm
                ArrayList<MyTransaction> dataList = dataStore.query(DataStore.COL_NAME_DATE + "=?", new String[]{String.valueOf(mt.mDate)});
                if (null == dataList || 0 >= dataList.size()) {
                    transList.add(mt);
                }
                else {
                    // TODO remove local data and sync to server data, but should improve the search algorithm
                    MyTransaction r = null;
                    for (MyTransaction old : syncList) {
                        if (mt.mDate == old.mDate) {
                            r = old;
                        }
                    }
                    if (null != r) {
                        syncList.remove(r);
                    }
                    syncList.add(mt);
                }
            }
        }

        // sync to DataStore
        // 1. update syncList
        for (MyTransaction mt : syncList) {
            if (null != mt) {
                mt.mIsSynced = true;
            }
        }
        dataStore.bucketUpdate(syncList);

        // 2. new from transList
        dataStore.bucketInsert(transList);

        if (null != listener) {
            // Get all transactions from DataStore
            // and filter deleted items
            listener.OnSyncCompleted(filterDeleted(dataStore.query(null, null)));
        }
    }

    private ArrayList<MyTransaction> filterDeleted(ArrayList<MyTransaction> mtList) {
        ArrayList<MyTransaction> removeList = new ArrayList<MyTransaction>();
        for (MyTransaction mt : mtList) {
            if (true == mt.mDeleted) {
                removeList.add(mt);
                Log.d(TAG, "deleted " + mt.toString());
            }
        }
        mtList.removeAll(removeList);

        return mtList;
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
