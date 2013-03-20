package com.allplayers.android;

import java.util.ArrayList;

import com.allplayers.objects.GroupData;
import com.allplayers.rest.RestApiV1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class GroupsFragment extends ListFragment {
    private ArrayList<GroupData> groupList;
    private boolean hasGroups = false, loadMore = true;
    private String jsonResult;
    private int pageNumber = 0;
    private int currentAmountShown = 0;
    private ArrayAdapter<String> adapter;
    private ProgressBar loadingMore;
    private int intendedGroups;

    private Activity parentActivity;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = this.getActivity();
        groupList = new ArrayList<GroupData>();
        adapter = new ArrayAdapter<String>(parentActivity, android.R.layout.simple_list_item_1);

        loadingMore = new ProgressBar(parentActivity);

        GetUserGroupsTask helper = new GetUserGroupsTask();
        helper.execute();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().addFooterView(loadingMore, null, false);
        setListAdapter(adapter);

        getListView().setOnScrollListener(new OnScrollListener() {
            private int visibleThreshold = 2;
            private int previousTotal = 0;
            private boolean loading = true;
            public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (loadMore && !loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    new GetUserGroupsTask().execute();
                    loading = true;
                }
            }
            @Override
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
            }

        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (hasGroups && position < groupList.size()) {
            //Display the group page for the selected group
            Intent intent = (new Router(parentActivity)).getGroupPageActivityIntent(groupList.get(position));
            startActivity(intent);
        }
    }

    /** Populates the list of groups to display to the UI thread. */
    protected void updateGroupData() {
        if (!groupList.isEmpty()) {

            // Counter to check if a full 8 new groups were loaded.
            int counter = 0;
            for (int i = currentAmountShown; i < groupList.size(); i++) {
                adapter.add(groupList.get(currentAmountShown).getTitle());
                currentAmountShown++;
                counter++;
            }

            // If we did not load 8 groups, we are at the end of the list, so signal
            // not to try to load more groups.
            if (counter < 8) {
                loadMore = false;
                getListView().removeFooterView(loadingMore);
            }

            hasGroups = true;
            // Check for default of no groups to display.
            if (adapter.getPosition("no groups to display") >= 0) {
                adapter.remove("no groups to display");
            }
        } else {
            hasGroups = false;
            if (adapter.getPosition("no groups to display") < 0) {
                adapter.add("no groups to display");
            }
            getListView().removeFooterView(loadingMore);
        }
    }

    /**
     * Fetches the groups a user belongs to and stores the data locally.
     */
    public class GetUserGroupsTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... args) {
            return RestApiV1.getUserGroups(pageNumber++ * 8, 8);
        }

        protected void onPostExecute(String jsonResult) {
            GroupsFragment.this.jsonResult += jsonResult;
            GroupsMap groups = new GroupsMap(jsonResult);
            groupList.addAll(groups.getGroupData());
            updateGroupData();
        }
    }
}
