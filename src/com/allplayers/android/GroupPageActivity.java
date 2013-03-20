
package com.allplayers.android;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.GroupData;
import com.allplayers.objects.GroupMemberData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupPageActivity extends AllplayersSherlockActivity {

    private GroupData group;
    private ArrayList<GroupMemberData> membersList;
    private ActionBar actionbar;
    private SideNavigationView sideNavigationView;

    /**
     * Called when the activity is first created, this creates the Action Bar
     * and sets up the Side Navigation Menu as well as assigning some variables.
     *
     * @param savedInstanceState: Saved data from the last instance of the
     *            activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        group = (new Router(this)).getIntentGroup();

        setButtonState(group.getUUID());

        setContentView(R.layout.grouppage);

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setDisplayShowTitleEnabled(false);

        TextView title = new TextView(this);
        title.setText(group.getTitle());
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.WHITE);
        title.setLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setPadding(0, 15, 0, 0);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
        actionbar.setCustomView(title, params);
        actionbar.setDisplayShowCustomEnabled(true);

        sideNavigationView = (SideNavigationView) findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        GetRemoteImageTask helper = new GetRemoteImageTask();
        helper.execute(group.getLogo());
        new GetGroupLocationTask().execute(group.getUUID());
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
     * Checks if the user is a member of the group in order to determine if they
     * should have access to the buttons provided to view members, events, and
     * photos.
     *
     * @param group_uuid: The uuid of the group to be checked.
     */
    private void setButtonState(String group_uuid) {

        GetGroupMembersByGroupIdTask helper = new GetGroupMembersByGroupIdTask();
        helper.execute(group_uuid);
    }

    /**
     * Gets a remote image using a REST call.
     */
    public class GetRemoteImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... logoURL) {

            return RestApiV1.getRemoteImage(logoURL[0]);
        }

        protected void onPostExecute(Bitmap logo) {

            ImageView imView = (ImageView) findViewById(R.id.groupLogo);
            imView.setImageBitmap(logo);
        }
    }

    public class GetGroupLocationTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... group_uuid) {
            return RestApiV1.getGroupInformationByGroupId(group_uuid[0]);
        }

        protected void onPostExecute(String jsonResult) {
            try {
                JSONObject groupInfo = new JSONObject(jsonResult);
                group.setZip(groupInfo.getJSONObject("location").getString("zip"));
                group.setLatLon(groupInfo.getJSONObject("location").getString("latitude"), groupInfo.getJSONObject("location").getString("longitude"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /*
     * Checks if the user is a group member. If the user is a group member the
     * group page interface is set up.
     */
    public class GetGroupMembersByGroupIdTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... group_uuid) {
            return RestApiV1.getGroupMembersByGroupId(group_uuid[0]);
        }

        protected void onPostExecute(String jsonResult) {
            boolean isMember = false;
            boolean isLoggedIn = RestApiV1.isLoggedIn();
            GroupMembersMap groupMembers = new GroupMembersMap(jsonResult);
            membersList = groupMembers.getGroupMemberData();
            String currentUUID = RestApiV1.getCurrentUserUUID();
            for (int i = 0; i < membersList.size(); i++) {
                if (membersList.get(i).getUUID().equals(currentUUID)) {
                    isMember = true;
                    break;
                }
            }

            final ImageButton groupMembersButton = (ImageButton) findViewById(R.id.groupMembersButton);
            if (isMember && isLoggedIn) groupMembersButton.setVisibility(View.VISIBLE);
            groupMembersButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = (new Router(GroupPageActivity.this))
                                    .getGroupMembersActivityIntent(group);
                    startActivity(intent);
                }
            });

            final ImageButton groupEventsButton = (ImageButton) findViewById(R.id.groupEventsButton);
            if (isLoggedIn) groupEventsButton.setVisibility(View.VISIBLE);
            groupEventsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = (new Router(GroupPageActivity.this))
                                    .getGroupEventsActivityIntent(group);
                    startActivity(intent);
                }
            });

            final ImageButton groupPhotosButton = (ImageButton) findViewById(R.id.groupPhotosButton);
            if (isLoggedIn) groupPhotosButton.setVisibility(View.VISIBLE);
            groupPhotosButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = (new Router(GroupPageActivity.this))
                                    .getGroupAlbumsActivityIntent(group);
                    startActivity(intent);
                }
            });

            final ImageButton groupLocationButton = (ImageButton) findViewById(R.id.groupLocationButton);
            if (isLoggedIn) groupLocationButton.setVisibility(View.VISIBLE);
            groupLocationButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = (new Router(GroupPageActivity.this))
                                    .getGroupLocationActivityIntent(group);
                    startActivity(intent);
                }
            });
        }
    }
}
