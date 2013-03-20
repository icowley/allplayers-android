package com.allplayers.android;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.MessageData;
import com.allplayers.objects.MessageThreadData;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MessageThread extends AllplayersSherlockListActivity {
    private ArrayList<MessageThreadData> messageThreadList;
    private boolean hasMessages = false;
    private String jsonResult = "";
    private int threadIDInt;
    private ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);
    private MessageData message;
    private SideNavigationView sideNavigationView;
    private ActionBar actionbar;

    /**
     * Called when the activity is first created, this sets up some variables,
     * creates the Action Bar, and sets up the Side Navigation Menu.
     * @param savedInstanceState: Saved data from the last instance of the
     * activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        message = (new Router(this)).getIntentMessage();
        String threadID = message.getThreadID();

        setContentView(R.layout.members_list);

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle("Messages");

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        PutAndGetMessagesTask helper = new PutAndGetMessagesTask();
        helper.execute(threadID);
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

    /**
     * Listener for the list on the page.
     * @param l
     * @param v
     * @param position: The position in the list of the clicked item.
     * @param id: The id of the list item that was clicked.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);

        if (hasMessages) {
            Intent intent = (new Router(this)).getMessageViewSingleIntent(message, messageThreadList.get(position));
            startActivity(intent);
        }
    }

    /**
     * An async task containing the REST calls needed to populate the messages
     * list.
     */
    public class PutAndGetMessagesTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... threadID) {

            threadIDInt = Integer.parseInt(threadID[0]);

            RestApiV1.putMessage(threadIDInt, 0, "");

            jsonResult = RestApiV1.getUserMessagesByThreadId(threadID[0]);

            return jsonResult;
        }

        /**
         * Gets a user's sent and received messages and puts this data into the
         * user's message thread.
         * @param jsonResult: The json result containing the data for the user's
         * sent and received messages in this thread.
         */
        protected void onPostExecute(String jsonResult) {

            HashMap<String, String> map;
            MessageThreadMap messages = new MessageThreadMap(jsonResult);
            messageThreadList = messages.getMessageThreadData();

            actionbar.setSubtitle("Thread started by " + messageThreadList.get(0).getSenderName());

            Collections.sort(messageThreadList, new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    MessageThreadData m1 = (MessageThreadData) o1;
                    MessageThreadData m2 = (MessageThreadData) o2;
                    return m2.getTimestampString().compareToIgnoreCase(m1.getTimestampString());
                }
            });

            if (!messageThreadList.isEmpty()) {
                hasMessages = true;
                for (int i = 0; i < messageThreadList.size(); i++) {
                    map = new HashMap<String, String>();
                    map.put("line1", messageThreadList.get(i).getMessageBody());
                    map.put("line2", "From: " + messageThreadList.get(i).getSenderName() + " - " + messageThreadList.get(i).getDateString());
                    list.add(map);
                }
            } else {
                hasMessages = false;

                map = new HashMap<String, String>();
                map.put("line1", "You have no new messages.");
                map.put("line2", "");
                list.add(map);
            }

            String[] from = { "line1", "line2" };

            int[] to = { android.R.id.text1, android.R.id.text2 };

            SimpleAdapter adapter = new SimpleAdapter(MessageThread.this, list, android.R.layout.simple_list_item_2, from, to);
            setListAdapter(adapter);
        }
    }
}