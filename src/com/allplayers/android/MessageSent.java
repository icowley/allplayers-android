package com.allplayers.android;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockListActivity;
import com.allplayers.objects.MessageData;
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

public class MessageSent extends AllplayersSherlockListActivity {

    private ArrayList<MessageData> messageList;
    private boolean hasMessages;
    private String jsonResult = "";
    private ActionBar actionbar;
    private SideNavigationView sideNavigationView;

    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.members_list);

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle("Messages");
        actionbar.setSubtitle("Sent");

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        //check local storage
        if (LocalStorage.getTimeSinceLastModification("Sentbox") / 1000 / 60 < 15) { //more recent than 15 minutes
            jsonResult = LocalStorage.readSentbox(getBaseContext());
            HashMap<String, String> map;

            MessagesMap messages = new MessagesMap(jsonResult);
            messageList = messages.getMessageData();

            if (!messageList.isEmpty()) {
                hasMessages = true;

                for (int i = 0; i < messageList.size(); i++) {
                    map = new HashMap<String, String>();
                    map.put("line1", messageList.get(i).getSubject());
                    map.put("line2", "Last sent from: " + messageList.get(i).getLastSender());
                    list.add(map);
                }
            } else {
                hasMessages = false;

                map = new HashMap<String, String>();
                map.put("line1", "You have no sent messages.");
                map.put("line2", "");
                list.add(map);
            }

            String[] from = { "line1", "line2" };

            int[] to = { android.R.id.text1, android.R.id.text2 };

            SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);
            setListAdapter(adapter);
        } else {
            GetUserSentBoxTask helper = new GetUserSentBoxTask();
            helper.execute();
        }
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

        if (hasMessages) {
            // Go to the message thread.
            Intent intent = (new Router(MessageSent.this)).getMessageThreadIntent(messageList.get(position));
            startActivity(intent);
        }
    }

    /*
     * Gets a user's sent mail box and populates a hash map with the data.
     */
    public class GetUserSentBoxTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... Args) {
            return RestApiV1.getUserSentBox();
        }

        protected void onPostExecute(String jsonResult) {
            LocalStorage.writeSentbox(getBaseContext(), jsonResult, false);
            HashMap<String, String> map;

            MessagesMap messages = new MessagesMap(jsonResult);
            messageList = messages.getMessageData();

            if (!messageList.isEmpty()) {
                hasMessages = true;

                for (int i = 0; i < messageList.size(); i++) {
                    map = new HashMap<String, String>();
                    map.put("line1", messageList.get(i).getSubject());
                    map.put("line2", "Last sent from: " + messageList.get(i).getLastSender());
                    list.add(map);
                }
            } else {
                hasMessages = false;

                map = new HashMap<String, String>();
                map.put("line1", "You have no sent messages.");
                map.put("line2", "");
                list.add(map);
            }

            String[] from = { "line1", "line2" };

            int[] to = { android.R.id.text1, android.R.id.text2 };

            SimpleAdapter adapter = new SimpleAdapter(MessageSent.this, list, android.R.layout.simple_list_item_2, from, to);
            setListAdapter(adapter);
        }
    }
}