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

public class SearchGroupsListActivity extends ListActivity {
    private ArrayList<GroupData> groupList;
    private boolean hasGroups = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Router router = new Router(this);
        String query = router.getIntentSearchQuery();
        int zipcode = router.getIntentSearchZipcode();
        int distance = router.getIntentSearchDistance();

        SearchGroupsTask helper = new SearchGroupsTask();
        helper.execute(query, zipcode, distance);
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
        if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU) {
            startActivity(new Intent(SearchGroupsListActivity.this, FindGroupsActivity.class));
        }

        return super.onKeyUp(keyCode, event);
    }

    /*
     * Searches for a group using a rest call and the user's specified
     *      parameters.
     */
    public class SearchGroupsTask extends AsyncTask<Object, Void, String> {
        protected String doInBackground(Object... args) {
            return RestApiV1.searchGroups((String)args[0], (Integer)args[1], (Integer)args[2]);
        }

        protected void onPostExecute(String jsonResult) {
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

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchGroupsListActivity.this,
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);
        }
    }
}