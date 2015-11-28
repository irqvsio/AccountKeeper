package com.aply.accountkeeper.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Local data provider implemented by SQLite to store all transaction data.
 */
public class SQLiteDataStore extends DataStore {
    private static final String TAG = "SQLiteDataStore";

    private SqlHelper mSqlHelper;

    public SQLiteDataStore(Context context) {
        super(context);
        mSqlHelper = new SqlHelper(context);
    }

    public synchronized void insert(MyTransaction newTransaction) {
        mSqlHelper.insert(newTransaction);
    }

    public synchronized void bucketInsert(ArrayList<MyTransaction> list) {
        mSqlHelper.bucketInsert(list);
    }

    public synchronized void update(MyTransaction updateTransaction) {
        mSqlHelper.update(updateTransaction);
    }

    public synchronized void bucketUpdate(ArrayList<MyTransaction> list) {
        mSqlHelper.bucketUpdate(list);
    }

    public synchronized ArrayList<MyTransaction> query(String selection, String[] selectionArgs) {
        return mSqlHelper.query(selection, selectionArgs);
    }

    @Override
    public synchronized void close() {
        mSqlHelper.close();
    }

    @Override
    public synchronized boolean isClosed() {
        return mSqlHelper.isClosed();
    }

    private class SqlHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "trans.db";
        private static final int DB_VERSION = 1;

        private static final String TABLE_NAME_TRANS = "trans_table";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_TRANS + " ("
                + "_id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
                + COL_NAME_GUID + " TEXT,"
                + COL_NAME_DATE + " LONG,"
                + COL_NAME_VALUE + " DOUBLE,"
                + COL_NAME_KIND + " TEXT,"
                + COL_NAME_DEL + " INTEGER,"
                + COL_NAME_SYNC + " INTEGER"
                + ")";
        private static final String SQL_CREATE_INDEX =
                "CREATE INDEX IDX_DATE ON " + TABLE_NAME_TRANS + " (guid, date)";
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME_TRANS;

        private boolean mIsClosed;

        public SqlHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            mIsClosed = false;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_INDEX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public synchronized void update(MyTransaction newTransaction) {
            if (mIsClosed || null == newTransaction) return;

            String key = String.valueOf(newTransaction.mDate);

            SQLiteDatabase db = mSqlHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_NAME_GUID, newTransaction.mGuid);
            contentValues.put(COL_NAME_DATE, newTransaction.mDate);
            contentValues.put(COL_NAME_VALUE, newTransaction.mValue);
            contentValues.put(COL_NAME_KIND, newTransaction.mKind);
            contentValues.put(COL_NAME_DEL, (newTransaction.mDeleted ? 1 : 0));
            contentValues.put(COL_NAME_SYNC, (newTransaction.mIsSynced? 1:0));
            int result = db.update(TABLE_NAME_TRANS,
                    contentValues,
                    COL_NAME_DATE + "=?",
                    new String[]{key});

            Log.d(TAG, "update : " + result + " key=" + key);


            onSharedPreferenceChanged(key);
        }

        public synchronized void bucketUpdate(ArrayList<MyTransaction> list) {
            if (mIsClosed || null == list || 0 >= list.size()) {
                return;
            }

            SQLiteDatabase db = mSqlHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                String update = "UPDATE " + TABLE_NAME_TRANS + " SET ";
                String where = " WHERE " + COL_NAME_DATE + "=";
                for (MyTransaction mt : list) {
                    String key = String.valueOf(mt.mDate);

                    String sql = update
                            + " " + COL_NAME_GUID + "='" + mt.mGuid + "',"
                            + " " + COL_NAME_DATE + "='" + mt.mDate + "',"
                            + " " + COL_NAME_VALUE + "='" + mt.mValue + "',"
                            + " " + COL_NAME_KIND + "='" + mt.mKind + "',"
                            + " " + COL_NAME_DEL + "='" + (mt.mDeleted? 1:0) + "',"
                            + " " + COL_NAME_SYNC + "='" + (mt.mIsSynced? 1:0) + "'"
                            + where + "'" + key +"'";
                    db.execSQL(sql);
                    Log.d(TAG, "update : " + sql);
                }
                Log.d(TAG, "update : ------");
                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
            }

            onSharedPreferenceChanged(null);
        }

        public synchronized void insert(MyTransaction newTransaction) {
            if (mIsClosed || null == newTransaction) return;

            SQLiteDatabase db = mSqlHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_NAME_GUID, newTransaction.mGuid);
            contentValues.put(COL_NAME_DATE, newTransaction.mDate);
            contentValues.put(COL_NAME_VALUE, newTransaction.mValue);
            contentValues.put(COL_NAME_KIND, newTransaction.mKind);
            contentValues.put(COL_NAME_DEL, (newTransaction.mDeleted? 1:0));
            contentValues.put(COL_NAME_SYNC, (newTransaction.mIsSynced? 1:0));
            long result = db.insert(TABLE_NAME_TRANS, null, contentValues);

            Log.d(TAG, "insert : " + result + " key=" + newTransaction.mDate);

            onSharedPreferenceChanged(String.valueOf(newTransaction.mDate));
        }

        public synchronized void bucketInsert(ArrayList<MyTransaction> list) {
            if (mIsClosed || null == list || 0 >= list.size()) {
                return;
            }

            SQLiteDatabase db = mSqlHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                String insert = "INSERT INTO " + TABLE_NAME_TRANS
                        + " (" + COL_NAME_GUID
                        + ", " + COL_NAME_DATE
                        + ", " + COL_NAME_VALUE
                        + ", " + COL_NAME_KIND
                        + ", " + COL_NAME_DEL
                        + ", " + COL_NAME_SYNC + ") VALUES (";
                for (MyTransaction mt : list) {
                    String sql = insert + "'"
                            + mt.mGuid + "','"
                            + mt.mDate + "','"
                            + mt.mValue + "','"
                            + mt.mKind + "','"
                            + (mt.mDeleted? 1:0) + "','"
                            + (mt.mIsSynced? 1:0) + "'"
                            + ")";
                    db.execSQL(sql);
                    Log.d(TAG, "insert : " + sql);
                }
                Log.d(TAG, "insert : ------");
                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
            }

            onSharedPreferenceChanged(null);
        }

        public synchronized ArrayList<MyTransaction> query(String selection, String[] selectionArgs) {
            SQLiteDatabase db = mSqlHelper.getReadableDatabase();
            Cursor cursor = db.query(TABLE_NAME_TRANS,
                    new String[]{COL_NAME_GUID, COL_NAME_DATE, COL_NAME_VALUE, COL_NAME_KIND, COL_NAME_DEL, COL_NAME_SYNC},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    COL_NAME_DATE + " DESC");

            ArrayList<MyTransaction> list = new ArrayList<MyTransaction>();
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    do {
                        MyTransaction mt = new MyTransaction(
                                cursor.getString(cursor.getColumnIndex(COL_NAME_GUID)),
                                cursor.getLong(cursor.getColumnIndex(COL_NAME_DATE)),
                                cursor.getDouble(cursor.getColumnIndex(COL_NAME_VALUE)),
                                cursor.getString(cursor.getColumnIndex(COL_NAME_KIND)),
                                1==cursor.getInt(cursor.getColumnIndex(COL_NAME_DEL)),
                                1==cursor.getInt(cursor.getColumnIndex(COL_NAME_SYNC))
                        );
                        list.add(mt);

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            return list;
        }

        public synchronized void close() {
            super.close();
            mIsClosed = true;
        }

        public synchronized boolean isClosed() {
            return mIsClosed;
        }
    }
}
