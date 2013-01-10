package com.allplayers.android;

import com.allplayers.objects.MessageData;
import com.allplayers.objects.MessageThreadData;
import com.allplayers.rest.RestApiV1;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MessageThread extends ListActivity {
    private ArrayList<MessageThreadData> messageThreadList;
    private boolean hasMessages = false;
    private String jsonResult = "";
    private int threadIDInt;

    ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(2);
    private MessageData message;

    /** Called when the activity is first created. */
    @SuppressWarnings( { "unchecked", "rawtypes" })
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        message = (new Router(this)).getIntentMessage();
        String threadID = message.getThreadID();

        PutAndGetMessagesTask helper = new PutAndGetMessagesTask();
        helper.execute(threadID);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (hasMessages) {
            Intent intent = (new Router(this)).getMessageViewSingleIntent(message, messageThreadList.get(position));
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.markreadmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.reply: {
            Intent intent = (new Router(this)).getMessagReplyIntent(message);
            startActivity(intent);
            return true;
        }
        case R.id.markRead: {
            RestApiV1.putMessage(threadIDInt, 1, "");
            startActivity(new Intent(MessageThread.this, MessageInbox.class));
            finish();
            return true;
        }
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public class PutAndGetMessagesTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... threadID) {
            threadIDInt = Integer.parseInt(threadID[0]);
            RestApiV1.putMessage(threadIDInt, 0, "");
            jsonResult = RestApiV1.getUserMessagesByThreadId(threadID[0]);
            return jsonResult;
        }

        /*
         * Gets a user's sent and received messages and puts this data into the
         *      user's message thread.
         */
        protected void onPostExecute(String jsonResult) {
            HashMap<String, String> map;

            MessageThreadMap messages = new MessageThreadMap(jsonResult);
            messageThreadList = messages.getMessageThreadData();

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