package com.allplayers.android.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockListActivity;
import com.allplayers.android.EventsActivity;
import com.allplayers.android.FindGroupsActivity;
import com.allplayers.android.GroupsActivity;
import com.allplayers.android.Login;
import com.allplayers.android.MessageActivity;
import com.allplayers.android.PhotosActivity;
import com.allplayers.android.R;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.ISideNavigationCallback;

public class AllplayersSherlockListActivity extends SherlockListActivity implements ISideNavigationCallback {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
     * Helper method for onSideNavigationItemClick. Starts the passed in
     * activity.
     *
     * @param activity: The activity to be started.
     */
    @SuppressWarnings("rawtypes")
    protected void invokeActivity(Class activity) {

        Intent intent = new Intent(this, activity);
        startActivity(intent);

        overridePendingTransition(0, 0); // Disables new activity animation.
    }

    /**
     * Opens the search screen.
     */
    protected void search() {

        startActivity(new Intent(this, FindGroupsActivity.class));
    }

    /**
     * Logs the user out of the application.
     */
    protected void logOut() {

        LogOutTask helper = new LogOutTask();
        helper.execute();

        AccountManager manager = AccountManager.get(this.getBaseContext());
        Account[] accounts = manager.getAccountsByType("com.allplayers.android");

        if (accounts.length == 1) {
            manager.removeAccount(accounts[0], null, null);
        }

        startActivity(new Intent(this, Login.class));
        finish();
    }

    /**
     * Refreshes the current activity to update information.
     */
    protected void refresh() {

        finish();
        startActivity(getIntent());
    }

    /**
     * Helper class to handle the network call needed to log out asynchronously.
     */
    protected class LogOutTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... args) {

            RestApiV1.logOut();
            return null;
        }
    }
}
