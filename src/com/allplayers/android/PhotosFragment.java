package com.allplayers.android;

import java.util.ArrayList;

import com.allplayers.objects.AlbumData;
import com.allplayers.objects.GroupData;
import com.allplayers.rest.RestApiV1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PhotosFragment extends ListFragment {
    private int groupCounter = 0;
    private int totalNumOfGroups = 0;
    private ArrayList<AlbumData> albumList = new ArrayList<AlbumData>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String jsonResult;

        if (LocalStorage.getTimeSinceLastModification("UserAlbums") / 1000 / 60 < 60) { //more recent than 60 minutes
            AlbumsMap albums;
            ArrayList<AlbumData> newAlbumList;

            String storedAlbums = LocalStorage.readUserAlbums(this.getActivity().getBaseContext());

            String[] storedAlbumsList = storedAlbums.split("\n");

            for (int i = 0; i < storedAlbumsList.length; i++) {
                albums = new AlbumsMap(storedAlbumsList[i]);
                newAlbumList = albums.getAlbumData();

                if (!newAlbumList.isEmpty()) {
                    for (int j = 0; j < newAlbumList.size(); j++) {
                        albumList.add(newAlbumList.get(j));
                    }
                }
            }
        } else {
            //check local storage
            if (LocalStorage.getTimeSinceLastModification("UserGroups") / 1000 / 60 < 60) { //more recent than 60 minutes
                jsonResult = LocalStorage.readUserGroups(this.getActivity().getBaseContext());
                populateGroupAlbums(jsonResult);
            } else {
                GetUserGroupsTask helper = new GetUserGroupsTask();
                helper.execute();
            }
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (!albumList.isEmpty()) {
            // Display the photos for the selected album
            Intent intent = (new Router(this.getActivity())).getAlbumPhotosActivityIntent(albumList.get(position));
            startActivity(intent);
        }
    }

    /**
     * Uses the json result passed in, and populates the UI with a list of albums from the
     * user's groups.
     * @param jsonResult
     */
    protected void populateGroupAlbums(String jsonResult) {
        GroupsMap groups = new GroupsMap(jsonResult);
        ArrayList<GroupData> groupList = groups.getGroupData();

        if (!groupList.isEmpty()) {
            String group_uuid;
            totalNumOfGroups = groupList.size();
            System.out.println("Total num of groups is " + totalNumOfGroups);
            LocalStorage.writeUserAlbums(this.getActivity().getBaseContext(), "", true);

            for (int i = 0; i < totalNumOfGroups; i++) {
                group_uuid = groupList.get(i).getUUID();
                GetGroupAlbumsByGroupIdTask helper = new GetGroupAlbumsByGroupIdTask();
                helper.execute(group_uuid);
            }
        } else {
            String[] values = new String[] {"no albums to display"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(PhotosFragment.this.getActivity(),
                    android.R.layout.simple_list_item_1, values);
            setListAdapter(adapter);
        }
    }

    /*
     * Gets a user's groups using a rest call.
     */
    public class GetUserGroupsTask extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... args) {
            return RestApiV1.getUserGroups(0, 0);
        }

        protected void onPostExecute(String jsonResult) {
            populateGroupAlbums(jsonResult);
        }
    }

    /*
     * Gets a group's photo albums using a rest call.
     */
    public class GetGroupAlbumsByGroupIdTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... group_uuid) {
            groupCounter++;
            System.out.println("COUNTER = " + groupCounter);
            return RestApiV1.getGroupAlbumsByGroupId(group_uuid[0]);
        }

        protected void onPostExecute(String jsonResult) {
            AlbumsMap albums;
            ArrayList<AlbumData> newAlbumList;
            albums = new AlbumsMap(jsonResult);
            newAlbumList = albums.getAlbumData();

            if (!newAlbumList.isEmpty()) {
                for (int j = 0; j < newAlbumList.size(); j++) {
                    albumList.add(newAlbumList.get(j));
                }
            }
            if (albumList.isEmpty() && totalNumOfGroups == groupCounter) {
                String[] values = new String[] {"no albums to display"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(PhotosFragment.this.getActivity(),
                        android.R.layout.simple_list_item_1, values);
                setListAdapter(adapter);
            } else {
                //Create a customized ArrayAdapter
                // TODO Fix to use only one adapter and push new items into it.
                AlbumAdapter adapter = new AlbumAdapter(PhotosFragment.this.getActivity().getApplicationContext(), R.layout.albumlistitem, albumList);
                setListAdapter(adapter);
            }
        }
    }
}
