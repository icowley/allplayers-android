package com.allplayers.android;

import com.allplayers.objects.AlbumData;
import com.allplayers.objects.GroupData;

import com.allplayers.rest.RestApiV1;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class GroupAlbumsActivity  extends ListActivity {
    private ArrayList<AlbumData> albumList;
    private TextView groupname;
    private TextView subtitle;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GroupData group = (new Router(this)).getIntentGroup();
        groupname = new TextView(this);
        groupname.setText(group.getTitle());
        groupname.setTextSize(30);
        subtitle = new TextView(this);
        subtitle.setText("\t\tPhoto Albums");
        subtitle.setTextSize(20);
        GetGroupAlbumsByGroupIdTask helper = new GetGroupAlbumsByGroupIdTask();
        helper.execute(group);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (!albumList.isEmpty()) {
            //Display the photos for the selected album
            Intent intent = (new Router(this)).getAlbumPhotosActivityIntent(albumList.get(position-2));
            startActivity(intent);
        }
    }

    /*
     * Gets the photo albums for a group by using the groups ID with a rest call.
     */
    public class GetGroupAlbumsByGroupIdTask extends AsyncTask<GroupData, Void, String> {

        protected String doInBackground(GroupData... groups) {
            return RestApiV1.getGroupAlbumsByGroupId(groups[0].getUUID());
        }

        protected void onPostExecute(String jsonResult) {
            AlbumsMap albums = new AlbumsMap(jsonResult);
            albumList = albums.getAlbumData();
            if (albumList.isEmpty()) {
                String[] values = new String[] {"no albums to display"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupAlbumsActivity.this,
                        android.R.layout.simple_list_item_1, values);
                setListAdapter(adapter);
            } else {
                //Create a customized ArrayAdapter
                AlbumAdapter adapter = new AlbumAdapter(getApplicationContext(), R.layout.albumlistitem, albumList);
                getListView().addHeaderView(groupname, null, false);
                getListView().addHeaderView(subtitle, null, false);
                setListAdapter(adapter);
            }
        }
    }
}