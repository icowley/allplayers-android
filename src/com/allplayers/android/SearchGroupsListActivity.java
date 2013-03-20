
package com.allplayers.android;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.GroupData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchGroupsListActivity extends AllplayersSherlockListActivity {

    private ArrayList<GroupData> groupList;
    private boolean hasGroups = false;
    private ActionBar actionbar;
    private SideNavigationView sideNavigationView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.search_groups_list);

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle("Search");

        sideNavigationView = (SideNavigationView) findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        Router router = new Router(this);
        String query = router.getIntentSearchQuery();
        int zipcode = router.getIntentSearchZipcode();
        int distance = router.getIntentSearchDistance();

        SearchGroupsTask helper = new SearchGroupsTask();
        helper.execute(query, zipcode, distance);
    }

    /**
     * Listener for the Action Bar Options Menu.
     *
     * @param item: The selected menu item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case android.R.id.home: {
            sideNavigationView.toggleMenu();
            return true;
        }

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Listener for the Side Navigation Menu.
     *
     * @param itemId: The ID of the list item that was selected.
     */
    @Override
    public void onSideNavigationItemClick(int itemId) {

        switch (itemId) {

        case R.id.side_navigation_menu_item1:
            invokeActivity(GroupsActivity.class);
            break;

        case R.id.side_navigation_menu_item2:
            invokeActivity(MessageActivity.class);
            break;

        case R.id.side_navigation_menu_item3:
            invokeActivity(PhotosActivity.class);
            break;

        case R.id.side_navigation_menu_item4:
            invokeActivity(EventsActivity.class);
            break;

        case R.id.side_navigation_menu_item5: {
            search();
            break;
        }

        case R.id.side_navigation_menu_item6: {
            logOut();
            break;
        }

        case R.id.side_navigation_menu_item7: {
            refresh();
            break;
        }

        default:
            return;
        }

        finish();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (hasGroups) {
            // Display the group page for the selected group
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
     * parameters.
     */
    public class SearchGroupsTask extends AsyncTask<Object, Void, String> {
        protected String doInBackground(Object... args) {
            return RestApiV1.searchGroups((String) args[0], (Integer) args[1], (Integer) args[2]);
        }

        protected void onPostExecute(String jsonResult) {
            GroupsMap groups = new GroupsMap(jsonResult);
            groupList = groups.getGroupData();

            if (groupList.size() == 1) {
                actionbar.setSubtitle("1 Result");
            } else {
                actionbar.setSubtitle(groupList.size() + " Results");
            }

            String[] values;

            if (!groupList.isEmpty()) {
                values = new String[groupList.size()];

                for (int i = 0; i < groupList.size(); i++) {
                    values[i] = groupList.get(i).getTitle();
                }

                hasGroups = true;
            } else {
                values = new String[] {
                    "no groups to display"
                };
                hasGroups = false;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchGroupsListActivity.this,
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);
        }
    }
}
