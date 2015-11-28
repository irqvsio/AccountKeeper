package com.aply.accountkeeper.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.aply.accountkeeper.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;


/**
 * Local data provider. Currently, use SharedPreferences to store all
 * transaction data. Please use {@code SQLiteDataStore}.
 */
@Deprecated
public class PreferenceDataStore extends DataStore {
    private static final String PREF_TRANSACTION = "trans.pref";

    private static final String KEY_SYNC_TIME = "key_sync_time";
//    private static final String KEY_TRANSACTION = "key_transaction";

    private SharedPreferences mSharedPreferences;

    public PreferenceDataStore(Context cxt) {
        super(cxt);
        mSharedPreferences = cxt.getSharedPreferences(PREF_TRANSACTION, Context.MODE_PRIVATE);
    }

    public synchronized void insert(MyTransaction newTransaction) {
        if (null == newTransaction) {
            return;
        }

        String newKey = Long.toString(newTransaction.mDate);
        if(true == Constants.DEBUG && true == mSharedPreferences.contains(newKey)) {
            Log.d("TAG", "Can't insert duo to have the same date, " + newTransaction.mDate);
        }

//        Set<String> keySet = mSharedPreferences.getStringSet(KEY_TRANSACTION, null);
//        if (null == keySet) {
//            keySet = new TreeSet<String>(new KeyComparator());
//        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String encodeTrans = MyTransaction.encode(newTransaction);
        editor.putString(newKey, encodeTrans);

//        keySet.add(newKey);
//        editor.putStringSet(KEY_TRANSACTION, keySet);

        editor.apply();

        onChanged(newKey);
    }

    public synchronized void bucketInsert(Collection<MyTransaction> list) {
        if (null == list) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (MyTransaction mt : list) {
            if (null == mt) {
                continue;
            }

            String newKey = Long.toString(mt.mDate);
            if(true == Constants.DEBUG && true == mSharedPreferences.contains(newKey)) {
                Log.d("TAG", "Can't insert duo to have the same date, " + mt.mDate);
            }

            editor.putString(newKey, MyTransaction.encode(mt));
        }
        editor.apply();

        onChanged(null);
    }

    public synchronized void update(MyTransaction updateTransaction) {
        if (null == updateTransaction) {
            return;
        }

        String key = Long.toString(updateTransaction.mDate);
        if(true == Constants.DEBUG && false == mSharedPreferences.contains(key)) {
            Log.d("TAG", "Can't update, " + updateTransaction.toString());
        }

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        String encodeTrans = MyTransaction.encode(updateTransaction);
        editor.putString(key, encodeTrans);

        editor.apply();

        onChanged(key);
    }

    public synchronized void bucketUpdate(Collection<MyTransaction> list) {
        if (null == list) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (MyTransaction mt : list) {
            if (null == mt) {
                continue;
            }

            String key = Long.toString(mt.mDate);
            if(true == Constants.DEBUG && false == mSharedPreferences.contains(key)) {
                Log.d("TAG", "Can't update, " + mt.mDate);
            }

            editor.putString(key, MyTransaction.encode(mt));
        }
        editor.apply();

        onChanged(null);
    }

    public synchronized ArrayList<MyTransaction> query(String selection, String[] selectionArgs) {
        Map<String, ?> allTrans =  mSharedPreferences.getAll();
        allTrans.remove(KEY_SYNC_TIME);

        ArrayList<MyTransaction> list = new ArrayList<MyTransaction>();

        for (Object obj : allTrans.values()) {
            if (null != obj) {
                MyTransaction t = MyTransaction.decode((String) obj);
                if (null != t) {
                    list.add(t);
                }
            }
        }

        // Sort by DESC
        Collections.sort(list, new KeyComparator());

        return list;
    }

    private class KeyComparator implements Comparator<MyTransaction> {
        @Override
        public int compare(MyTransaction lhs, MyTransaction rhs) {
            if (lhs == rhs) {
                return 0;
            }

            if (null == lhs || null == rhs) {
                return 0;
            }

            // Sort by DESC
            return (int) (rhs.mDate - lhs.mDate);
        }
    }
}
