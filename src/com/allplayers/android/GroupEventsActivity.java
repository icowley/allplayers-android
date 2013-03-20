package com.allplayers.android;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.EventData;
import com.allplayers.objects.GroupData;

import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupEventsActivity extends AllplayersSherlockListActivity {
    private ArrayList<EventData> eventsList;
    private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);
    private boolean hasEvents = false;
    private ActionBar actionbar;
    private SideNavigationView sideNavigationView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.events_list);

        GroupData group = (new Router(this)).getIntentGroup();

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle(group.getTitle());
        actionbar.setSubtitle("Events");

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        GetIntentGroupTask helper = new GetIntentGroupTask();
        helper.execute(group);
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

        if (hasEvents) {
            // Can be used to display map or full details.
            Intent intent = (new Router(this)).getEventDisplayActivityIntent(eventsList.get(position));
            startActivity(intent);
        }
    }

    /*
     * Gets a group's events using a rest call and places the data into a hash map.
     */
    public class GetIntentGroupTask extends AsyncTask<GroupData, Void, String> {

        protected String doInBackground(GroupData... groups) {
            return RestApiV1.getGroupEventsByGroupId(groups[0].getUUID());
        }

        protected void onPostExecute(String jsonResult) {
            EventsMap events = new EventsMap(jsonResult);
            eventsList = events.getEventData();

            HashMap<String, String> map;
            if (!eventsList.isEmpty()) {
                for (int i = 0; i < eventsList.size(); i++) {
                    map = new HashMap<String, String>();
                    map.put("line1", eventsList.get(i).getTitle());

                    String start = eventsList.get(i).getStartDateString();
                    map.put("line2", start);
                    list.add(map);
                }
                hasEvents = true;
            } else {
                map = new HashMap<String, String>();
                map.put("line1", "No events to display.");
                map.put("line2", "");
                list.add(map);
                hasEvents = false;
            }
            String[] from = {"line1", "line2"};
            int[] to = {android.R.id.text1, android.R.id.text2};
            SimpleAdapter adapter = new SimpleAdapter(GroupEventsActivity.this, list, android.R.layout.simple_list_item_2, from, to);
            setListAdapter(adapter);
        }
    }
}