package com.allplayers.android;

import com.allplayers.objects.GroupData;
import com.allplayers.rest.RestApiV1;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class GroupsActivity extends ListActivity {
    private ArrayList<GroupData> groupList;
    private boolean hasGroups = false;
    private String jsonResult;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check local storage
        if (LocalStorage.getTimeSinceLastModification("UserGroups") / 1000 / 60 < 60) { //more recent than 60 minutes
            jsonResult = LocalStorage.readUserGroups(getBaseContext());
            setGroupData();
        } else {
            //jsonResult = RestApiV1.getUserGroups();
            GetUserGroupsTask helper = new GetUserGroupsTask();
            helper.execute();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (hasGroups) {
            //Display the group page for the selected group
            Intent intent = (new Router(this)).getGroupPageActivityIntent(groupList.get(position));
            startActivity(intent);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            startActivity(new Intent(GroupsActivity.this, FindGroupsActivity.class));
        }

        return super.onKeyUp(keyCode, event);
    }

    /** Populates the list of groups to display to the UI thread. */
    protected void setGroupData() {
        GroupsMap groups = new GroupsMap(jsonResult);
        groupList = groups.getGroupData();

        String[] values;

        if (!groupList.isEmpty()) {
            values = new String[groupList.size()];

            for (int i = 0; i < groupList.size(); i++) {
                values[i] = groupList.get(i).getTitle();
            }

            hasGroups = true;
        } else {
            values = new String[] {"no groups to display"};
            hasGroups = false;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);
    }

    /*
     * Fetches the groups a user belongs to and stores the data locally.
     */
    public class GetUserGroupsTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... args) {
            return RestApiV1.getUserGroups();
        }

        protected void onPostExecute(String jsonResult) {
            GroupsActivity.this.jsonResult = jsonResult;
            LocalStorage.writeUserGroups(getBaseContext(), jsonResult, false);
            setGroupData();
        }
    }
}