package com.allplayers.android;

import java.io.IOException;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockMapActivity;
import com.allplayers.objects.EventData;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * TODO If maps are missing on device image, this activity will crash.
 */
public class EventDisplayActivity extends AllplayersSherlockMapActivity {
    private SideNavigationView sideNavigationView;

    /**
     * Called when the activity is first created, this sets up variables,
     * creates the Action Bar, and sets up the Side Navigation Menu.
     * @param savedInstanceState: Saved data from the last instance of the
     * activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventdetail);

        TextView eventInfo = (TextView)findViewById(R.id.eventInfo);
        MapView map = (MapView)findViewById(R.id.eventMap);
        map.setBuiltInZoomControls(true);

        EventData event = (new Router(this)).getIntentEvent();
        eventInfo.setText("Event Title: " + event.getTitle() + "\nDescription: " +
                          event.getDescription() + "\nCategory: " + event.getCategory() +
                          "\nStart: " + event.getStartDateString() + "\nEnd: " + event.getEndDateString());

        MapController mapController = map.getController();
        String lat = "";
        lat = event.getLatitude();
        String lon = "";
        lon = event.getLongitude();

        if (lat.equals("") || lon.equals("")) {
            map.setVisibility(View.GONE);
        } else {
            if (lat.equals("0.000000") || lon.equals("0.000000")) {
                Geocoder geo = new Geocoder(this);
                try {
                    List<Address> addr = geo.getFromLocationName(event.getZip(), 1);
                    lat = addr.get(0).getLatitude() + "";
                    lon = addr.get(0).getLongitude() + "";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            GeoPoint point = new GeoPoint((int)(Float.parseFloat(lat) * 1000000), (int)(Float.parseFloat(lon) * 1000000));
            mapController.setCenter(point);
            mapController.setZoom(15);

            List<Overlay> mapOverlays = map.getOverlays();
            Drawable drawable = this.getResources().getDrawable(R.drawable.mini_icon);
            mItemizedOverlay itemizedoverlay = new mItemizedOverlay(drawable, this);
            OverlayItem center = new OverlayItem(point, event.getTitle(), event.getZip());
            itemizedoverlay.addOverlay(center);
            mapOverlays.add(itemizedoverlay);
        }
        ActionBar actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle(event.getTitle());

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);
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
     * Checks if a route is displayed on the map (always returns false in this
     * implementation).
     */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}