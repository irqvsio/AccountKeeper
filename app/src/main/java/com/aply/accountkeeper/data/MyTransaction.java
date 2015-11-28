package com.aply.accountkeeper.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.bean_keeper.Proto.Transaction;

/**
 * Wrapper class to Transaction
 */
public class MyTransaction implements Parcelable {

    private static final String SEPARATOR = ",";

    public static String encode(MyTransaction myTrans) {
        return myTrans.mGuid
                + SEPARATOR + myTrans.mDate
                + SEPARATOR + myTrans.mValue
                + SEPARATOR + myTrans.mKind
                + SEPARATOR + myTrans.mDeleted
                + SEPARATOR + myTrans.mIsSynced;
    }

    public static MyTransaction decode(String str) {
        if (null == str) {
            return null;
        }

        String[] tokens = str.split(SEPARATOR);
        // TODO should be refine length
        if (6 != tokens.length) {
            return null;
        }

        MyTransaction myTrans = new MyTransaction();
        myTrans.mGuid = tokens[0];
        myTrans.mDate = Long.valueOf(tokens[1]);
        myTrans.mValue = Double.valueOf(tokens[2]);
        myTrans.mKind = tokens[3];
        myTrans.mDeleted = Boolean.valueOf(tokens[4]);
        myTrans.mIsSynced = Boolean.valueOf(tokens[5]);

        return myTrans;
    }

    public String mGuid;
    public long mDate;
    public double mValue;
    public String mKind;
    public Boolean mDeleted;

    public boolean mIsSynced;

    private MyTransaction() {

    }

    public MyTransaction(Transaction t) {
        mGuid = t.getGuid();
        mDate = t.getDate();
        mValue = t.getValue();
        mKind = t.getKind();
        mDeleted = t.getDeleted();

        mIsSynced = false;
    }

    public MyTransaction(String guid, double value, String kind) {
        long currTime = System.currentTimeMillis();
        mGuid = guid + currTime;
        mDate = currTime;
        mValue = value;
        mKind = kind;
        mDeleted = false;

        mIsSynced = false;
    }

    public MyTransaction(String guid, long date, double value, String kind, boolean deleted, boolean synced) {
        mGuid = guid;
        mDate = date;
        mValue = value;
        mKind = kind;
        mDeleted = deleted;

        mIsSynced = synced;
    }

    protected MyTransaction(Parcel in) {
        mGuid = in.readString();
        mDate = in.readLong();
        mValue = in.readDouble();
        mKind = in.readString();
        mDeleted = (in.readInt() == 1);
        mIsSynced = (in.readInt() == 1);
    }

    public Transaction Build() {
        return Transaction.newBuilder()
                .setGuid(mGuid)
                .setDate(mDate)
                .setValue(mValue)
                .setKind(mKind)
                .setDeleted(mDeleted)
                .build();
    }

    @Override
    public String toString() {
        return "GUID:" + mGuid
                + SEPARATOR + "Data:" + mDate
                + SEPARATOR + "Value:" + mValue
                + SEPARATOR + "Kind:" + mKind
                + SEPARATOR + "Deleted:" + mDeleted
                + SEPARATOR + "Synced:" + mIsSynced;
    }

    public static final Creator<MyTransaction> CREATOR = new Creator<MyTransaction>() {
        @Override
        public MyTransaction createFromParcel(Parcel in) {
            return new MyTransaction(in);
        }

        @Override
        public MyTransaction[] newArray(int size) {
            return new MyTransaction[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mGuid);
        dest.writeLong(mDate);
        dest.writeDouble(mValue);
        dest.writeString(mKind);
        dest.writeInt((mDeleted ? 1 : 0));
        dest.writeInt((mIsSynced ? 1 : 0));
    }
}
