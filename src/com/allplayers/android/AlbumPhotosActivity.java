package com.allplayers.android;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.rest.RestApiV1;
import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.AlbumData;
import com.allplayers.objects.PhotoData;
import com.devspark.sidenavigation.ISideNavigationCallback;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.AdapterView;

import java.util.ArrayList;

public class AlbumPhotosActivity extends AllplayersSherlockActivity {
    private ArrayList<PhotoData> photoList;
    private ArrayAdapter blankAdapter = null;
    private PhotoAdapter photoAdapter;
    private GridView grid;
    private ActionBar actionbar;
    private SideNavigationView sideNavigationView;

    /**
     * Called when the activity is first created, this sets up some variables,
     * creates the Action Bar, and sets up the Side Navigation Menu.
     * @param savedInstanceState: Saved data from the last instance of the
     * activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.albumdisplay);

        final AlbumData album = (new Router(this)).getIntentAlbum();
        photoList = new ArrayList<PhotoData>();
        grid = (GridView) findViewById(R.id.gridview);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (photoList.get(position) != null) {
                    // Display the group page for the selected group
                    Intent intent = (new Router(AlbumPhotosActivity.this)).getPhotoPagerActivityIntent(photoList.get(position));
                    intent.putExtra("album title", album.getTitle());
                    startActivity(intent);
                }
            }
        });

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle(album.getTitle());

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        new GetAlbumPhotosByAlbumIdTask().execute(album);
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
     * Creates an array adapter to store the album's photos.
     */
    public void setAdapter() {

        if (photoList.isEmpty()) {
            String[] values = new String[] {"no photos to display"};

            blankAdapter = new ArrayAdapter<String>(AlbumPhotosActivity.this,
                                                    android.R.layout.simple_list_item_1, values);
            grid.setAdapter(blankAdapter);
        } else {
            photoAdapter = new PhotoAdapter(getApplicationContext());
            grid.setAdapter(photoAdapter);
        }
    }

    /**
     * Aggregates the albums photos specified by a json string into a map.
     * @param jsonResult The album's photos.
     */
    public void updateAlbumPhotos(String jsonResult) {

        PhotosMap photos = new PhotosMap(jsonResult);
        photoList.addAll(photos.getPhotoData());

        setAdapter();

        if (photoList.size() != 0) {
            photoAdapter.addAll(photoList);
        }
    }

    /**
     * Gets the photos from an album specified by its album ID using a rest call.
     */
    public class GetAlbumPhotosByAlbumIdTask extends AsyncTask<AlbumData, Void, String> {

        /**
         * Gets the photos in a specified album using a rest call.
         */
        protected String doInBackground(AlbumData... album) {

            return RestApiV1.getAlbumPhotosByAlbumId(album[0].getUUID(), 0, 0);
        }

        /**
         * Calls a method to organize the fetched photos into a map.
         */
        protected void onPostExecute(String jsonResult) {

            updateAlbumPhotos(jsonResult);
        }
    }
}
