package com.nathanrjones.castkeys;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;
import com.google.cast.SessionError;

import java.io.IOException;

import static android.app.ActionBar.NAVIGATION_MODE_STANDARD;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, MediaRouteAdapter {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    private CastContext mCastContext;
    private MediaRouteButton mMediaRouteButton;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private MediaRouteStateChangeListener mRouteStateListener;

    private static final String APP_NAME = "af2828a5-5a82-4be6-960a-2171287aed09";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mMediaRouteButton = (MediaRouteButton) findViewById(R.id.action_cast);

//        mCastContext = new CastContext(getApplicationContext());
//        MediaRouteHelper.registerMinimalMediaRouteProvider(mCastContext, this);
//        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
//        mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector(
//                MediaRouteHelper.CATEGORY_CAST);
//        mMediaRouteButton.setRouteSelector(mMediaRouteSelector);
//        mMediaRouterCallback = new MyMediaRouterCallback();

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
//                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
//    }
//
//    @Override
//    protected void onStop() {
//        mMediaRouter.removeCallback(mMediaRouterCallback);
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        MediaRouteHelper.unregisterMediaRouteProvider(mCastContext);
//        mCastContext.dispose();
//        super.onDestroy();
//    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {



        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate( R.menu.main, menu );

            MenuItem mediaRouteItem = menu.findItem( R.id.action_cast );
            if (mediaRouteItem != null) {
                mMediaRouteButton = (MediaRouteButton) mediaRouteItem.getActionView();
            }

            mCastContext = new CastContext( getApplicationContext() );
            MediaRouteHelper.registerMinimalMediaRouteProvider( mCastContext, this );
            mMediaRouter = MediaRouter.getInstance( getApplicationContext() );
            mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector( MediaRouteHelper.CATEGORY_CAST );
            mMediaRouteButton.setRouteSelector( mMediaRouteSelector );
            mMediaRouterCallback = new MyMediaRouterCallback();

            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            MediaRouteHelper.requestCastDeviceForRoute(route);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            mSelectedDevice = null;
            mRouteStateListener = null;
        }
    }

    @Override
    public void onDeviceAvailable(CastDevice castDevice, String s, MediaRouteStateChangeListener mediaRouteStateChangeListener) {
        //setSelectedDevice(castDevice);
    }

    @Override
    public void onSetVolume(double v) {

    }

    @Override
    public void onUpdateVolume(double v) {

    }

    private void setSelectedDevice(CastDevice device) {
        mSelectedDevice = device;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
