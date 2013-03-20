package com.allplayers.android;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.PhotoData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PhotoPager extends AllplayersSherlockActivity {

    private ViewPager mViewPager;
    private PhotoPagerAdapter photoAdapter;
    private PhotoData currentPhoto;
    private int currentPhotoIndex;
    private SideNavigationView sideNavigationView;

    /**
     * Called when the activity is first created, this sets up some variables,
     * creates the Action Bar, and sets up the Side Navigation Menu.
     * @param savedInstanceState: Saved data from the last instance of the
     * activity.
     * @TODO The side navigation menu does NOT work due to conflicting views.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            currentPhotoIndex = savedInstanceState.getInt("photoToStart");
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_pager);

        currentPhoto = (new Router(this)).getIntentPhoto();
        mViewPager = new ViewPager(this);
        photoAdapter = new PhotoPagerAdapter(this, currentPhoto);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(photoAdapter);
        mViewPager.setCurrentItem(currentPhotoIndex);


        ActionBar actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle(getIntent().getStringExtra("album title"));

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
     * Called before placing the activity in a background state. Saves the
     * instance data for the activity to be used the next time onCreate() is
     * called.
     * @param icicle: The bundle to add to.
     */
    protected void onSaveInstanceState(Bundle icicle) {

        super.onSaveInstanceState(icicle);

        currentPhotoIndex = mViewPager.getCurrentItem();
        icicle.putInt("photoToStart", currentPhotoIndex);
    }

    /**
     * @TODO EDIT ME
     */
    public class PhotoPagerAdapter extends PagerAdapter {

        private Context mContext;
        private ImageView[] images;
        private List<PhotoData> photos;

        /**
         * @TODO EDIT ME
         * @param context:
         * @param item:
         */
        public PhotoPagerAdapter(Context context, PhotoData item) {

            mContext = context;
            photos = new ArrayList<PhotoData>();

            PhotoData temp = item;

            while (temp.previousPhoto() != null) {
                photos.add(0, temp.previousPhoto());
                temp = temp.previousPhoto();
            }

            if (currentPhotoIndex == 0) {
                currentPhotoIndex = photos.size();
            }

            photos.add(item);

            temp = item;

            while (temp.nextPhoto() != null) {
                photos.add(temp.nextPhoto());
                temp = temp.nextPhoto();
            }

            images = new ImageView[photos.size()];
        }

        /**
         * @TODO EDIT ME
         * @param collection:
         * @param position:
         */
        @Override
        public Object instantiateItem(View collection, int position) {

            ImageView image = new ImageView(PhotoPager.this);
            image.setImageResource(R.drawable.loading_image);

            if (images[position] != null) {
                ((ViewPager) collection).addView(images[position], 0);
                return images[position];
            }

            images[position] = image;

            new GetRemoteImageTask().execute(photos.get(position).getPhotoFull(), position);
            ((ViewPager) collection).addView(images[position], 0);

            return images[position];
        }

        /**
         * @TODO EDIT ME
         * @param container:
         * @param position:
         * @param object:
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            ((ViewPager) container).removeView((ImageView) object);
        }

        /**
         * @TODO Returns the size of the list of photos.
         */
        @Override
        public int getCount() {

            return photos.size();
        }

        /**
         * @TODO EDIT ME
         * @param view:
         * @param obj:
         */
        @Override
        public boolean isViewFromObject(View view, Object obj) {

            return view == obj;
        }

        /**
         * Get's a user's image using a rest call and displays it.
         */
        public class GetRemoteImageTask extends AsyncTask<Object, Void, Bitmap> {

            int index;

            /**
             * Gets the requested image using a REST call.
             * @param photoUrl: The URL of the photo to fetch.
             */
            protected Bitmap doInBackground(Object... photoUrl) {

                index = (Integer) photoUrl[1];

                return RestApiV1.getRemoteImage((String) photoUrl[0]);
            }

            /**
             * Adds the fetched image to an array of the album's images.
             * @param image: The image to be added.
             */
            protected void onPostExecute(Bitmap image) {

                images[index].setImageBitmap(image);
            }
        }
    }
}
