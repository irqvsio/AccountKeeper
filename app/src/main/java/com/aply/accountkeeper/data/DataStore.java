package com.aply.accountkeeper.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by aply_lai on 2015/11/1.
 */
public abstract class DataStore {
    private static final String TAG = "DataStore";

    public static final String COL_NAME_GUID = "guid";
    // TODO think if ther is more good key than date
    public static final String COL_NAME_DATE = "date";      // use as unique key
    public static final String COL_NAME_VALUE = "value";
    public static final String COL_NAME_KIND = "kind";
    public static final String COL_NAME_DEL = "deleted";
    public static final String COL_NAME_SYNC = "synced";

    /**
     * Interface to be implemented by objects that want to receive notification if data is updated
     */
    public interface Observer {
        /**
         * This method is called if there is any data change on the specified Observable object's.
         * @param key the key of change data
         */
        void update(String key);
    }

    private static final String PREF_TRANSACTION = "trans.pref";
    private static final String KEY_SYNC_TIME = "key_sync_time";

    private SharedPreferences mSharedPreferences;
    private ArrayList<Observer> mObservers;
    private boolean mIsClosed;

    protected DataStore(Context cxt) {
        mSharedPreferences = cxt.getSharedPreferences(PREF_TRANSACTION, Context.MODE_PRIVATE);
        mObservers = new ArrayList<Observer>();
    }

    public synchronized void setSyncTime(long time) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(KEY_SYNC_TIME, time);
        editor.apply();
    }

    public synchronized long getSyncTime() {
        long date = mSharedPreferences.getLong(KEY_SYNC_TIME, 0);
        if (0 == date) {
            GregorianCalendar cal = new GregorianCalendar(1970, 1, 1, 0, 0, 0);
            date = cal.getTimeInMillis();
        }
        return date;
    }

    public synchronized void addDataChangeObserver(Observer observer) {
        if (null == observer) {
            return;
        }
        mObservers.add(observer);
    }

    public synchronized void removeDataChangeObserver(Observer observer) {
        if (null == observer) {
            return;
        }
        mObservers.remove(observer);
    }

    protected synchronized void onSharedPreferenceChanged(String key) {
        for (Observer observer : mObservers) {
            observer.update(key);
        }
    }

    public abstract void insert(MyTransaction newTransaction);

    public abstract void bucketInsert(ArrayList<MyTransaction> list);

    public abstract void update(MyTransaction updateTransaction);

    public abstract void bucketUpdate(ArrayList<MyTransaction> list);

    public abstract ArrayList<MyTransaction> query(String selection, String[] selectionArgs);

    public synchronized void close() {
        mIsClosed = true;
    }

    public synchronized boolean isClosed() {
        return mIsClosed;
    }
}
