package com.aply.accountkeeper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import com.aply.accountkeeper.data.DataStore;
import com.aply.accountkeeper.data.PreferenceDataStore;
import com.aply.accountkeeper.data.MyTransaction;
import com.aply.accountkeeper.data.TransactionListAdapter;


/**
 * Main view (list view) to show all transactions without deleted items.
 * The user can create new transaction by FAB, get the sum value by "Sum"
 * in option menu, and also can sync data with backend setver by "Refresh"
 * in option menu. It also can edit or delete one transaction in context menu
 * by long press the item in list view.
 */
public class TransactionListFragment extends ListFragment implements PreferenceDataStore.Observer, AbsListView.OnScrollListener {

    private TransactionListAdapter mAdapter;

    public TransactionListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new TransactionListAdapter(getActivity());
        setListAdapter(mAdapter);

        MainActivity.getDataStore(getActivity()).addDataChangeObserver(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerForContextMenu(getListView());

        getListView().setOnScrollListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        FragmentActivity fActivity = getActivity();
        DataStore dataStore = MainActivity.getDataStore(fActivity);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MyTransaction currTrans = (MyTransaction) mAdapter.getItem(info.position);
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.menu_edit:
                EditTransactionFragment newTran = EditTransactionFragment.getInstance(EditTransactionFragment.EDIT_TYPE_EDIT, currTrans);
                newTran.show(fActivity.getSupportFragmentManager(), EditTransactionFragment.FRAGMENT_TAG);
                break;
            case R.id.menu_delete:
                currTrans.mDeleted = true;
                currTrans.mIsSynced = false;
                dataStore.update(currTrans);
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (R.id.action_sum == id) {
            DetailDialogFragment sumFragment = new DetailDialogFragment();

            Bundle arg = new Bundle();
            arg.putParcelableArrayList(DetailDialogFragment.KEY_ALL_TRANSACTION, mAdapter.getList());
            sumFragment.setArguments(arg);

            sumFragment.show(getActivity().getSupportFragmentManager(), DetailDialogFragment.FRAGMENT_TAG);
            return true;
        }
        else if (R.id.action_refresh == id) {
            mAdapter.refresh(getActivity(), true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        unregisterForContextMenu(getListView());
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.resume();
    }

    @Override
    public void onPause() {
        mAdapter.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
        MainActivity.getDataStore(getActivity()).removeDataChangeObserver(this);
    }

    @Override
    public void update(String key) {
        mAdapter.refresh(getActivity(), false);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
//        if (SCROLL_STATE_IDLE == scrollState) {
//            ((MainActivity) getActivity()).setFABVisibility(View.VISIBLE);
//        }
//        else {
//            ((MainActivity) getActivity()).setFABVisibility(View.GONE);
//        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
