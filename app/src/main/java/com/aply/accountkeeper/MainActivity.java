package com.aply.accountkeeper;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aply.accountkeeper.data.DataStore;
import com.aply.accountkeeper.data.PreferenceDataStore;
import com.aply.accountkeeper.data.SQLiteDataStore;

/**
 * Main activity to hold list view fragment and FAB button.
 */
public class MainActivity extends AppCompatActivity {

    private static final Object LOCKER = new Object();
    private static DataStore sDataStore;

    public static DataStore getDataStore(Context context) {
        synchronized(LOCKER) {
            if(null == sDataStore || sDataStore.isClosed()) {
//                sDataStore = new PreferenceDataStore(context);
                sDataStore = new SQLiteDataStore(context);
            }
        }
        return sDataStore;
    }

    private static void release() {
        synchronized(LOCKER) {
            if(null != sDataStore) {
                sDataStore.close();
                sDataStore = null;
            }
        }
    }

    public final static String GUID = "6ea4f8dd-382f-44a6-bc0a-91f3c6b8216b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.getDataStore(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditTransactionFragment newTran = EditTransactionFragment.getInstance(EditTransactionFragment.EDIT_TYPE_ADD, null);
                newTran.show(MainActivity.this.getSupportFragmentManager(), EditTransactionFragment.FRAGMENT_TAG);
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_main, new TransactionListFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

//    public void setFABVisibility(int visibility) {
//        findViewById(R.id.fab).setVisibility(visibility);
//    }
}
