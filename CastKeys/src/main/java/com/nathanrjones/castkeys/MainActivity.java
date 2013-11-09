package com.nathanrjones.castkeys;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.MediaProtocolMessageStream;
import com.google.cast.MediaRouteAdapter;
import com.google.cast.MediaRouteHelper;
import com.google.cast.MediaRouteStateChangeListener;
import com.google.cast.SessionError;

import org.json.JSONObject;
import java.io.IOException;

import static android.app.ActionBar.NAVIGATION_MODE_STANDARD;

public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, MediaRouteAdapter {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private EditText mMessageInput;

    private FragmentActivity mActivity;

    private CastContext mCastContext;
    private MediaRouteButton mMediaRouteButton;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private MediaRouteStateChangeListener mRouteStateListener;

    private ApplicationSession mSession;
    private MediaProtocolMessageStream mMediaMessageStream;
    private CastKeysMessageStream mCastKeysMessageStream;


    private String mAppName;
    private String mTheme;
    private static final String NRJ_APP_NAME = "af2828a5-5a82-4be6-960a-2171287aed09";

    private static final int POSITION_MAIN = 0;
    private static final int POSITION_SETTINGS = 1;
    private static final int POSITION_ABOUT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mCastContext = new CastContext( getApplicationContext() );
        MediaRouteHelper.registerMinimalMediaRouteProvider( mCastContext, this );

        mMediaRouter = MediaRouter.getInstance( getApplicationContext() );
        mMediaRouteSelector = MediaRouteHelper.buildMediaRouteSelector( MediaRouteHelper.CATEGORY_CAST );
        mMediaRouterCallback = new MediaRouterCallback();

        mMessageInput = (EditText) findViewById(R.id.message_input);
        if (mMessageInput != null) mMessageInput.addTextChangedListener(messageTextWatcher);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mActivity = this;

        mMessageInput = (EditText) findViewById(R.id.message_input);
        if (mMessageInput != null) mMessageInput.addTextChangedListener(messageTextWatcher);

        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mTheme = prefs.getString("pref_theme", "default");
        mAppName = prefs.getString("pref_app_name", NRJ_APP_NAME);

        if (mCastKeysMessageStream != null) {
            mCastKeysMessageStream.changeTheme(mTheme);
        }
    }

    @Override
    protected void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MediaRouteHelper.unregisterMediaRouteProvider(mCastContext);
        mCastContext.dispose();
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        if (position == POSITION_SETTINGS){ // Settings
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 0);
            return;
        }

        Fragment fragment;

        switch (position){
            case POSITION_MAIN:
                fragment = new MainFragment();
                break;
            case POSITION_ABOUT:
                fragment = new AboutFragment();
                break;
            default:
                fragment = new MainFragment();
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case POSITION_MAIN:
                mTitle = getString(R.string.title_section1);
                break;
            case POSITION_SETTINGS:
                mTitle = getString(R.string.title_section2);
                break;
            case POSITION_ABOUT:
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
            mMediaRouteButton = (MediaRouteButton) mediaRouteItem.getActionView();
            mMediaRouteButton.setRouteSelector( mMediaRouteSelector );

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

    private TextWatcher messageTextWatcher = new TextWatcher()
    {
        @Override
        public void onTextChanged(CharSequence s, int start,int before,int count){

            String newCharacters = s.toString();

            if (mCastKeysMessageStream != null) {
                mCastKeysMessageStream.changeTheme(mTheme);
                mCastKeysMessageStream.sendCastKeysMessage(newCharacters);
            }

        }
        @Override
        public void afterTextChanged(Editable s){

        }
        @Override
        public void beforeTextChanged(CharSequence s , int start,int count , int after){

        }

    };

    protected final void setMediaRouteButtonVisible() {
        mMediaRouteButton.setVisibility(mMediaRouter.isRouteAvailable(
                mMediaRouteSelector, 0) ? View.VISIBLE : View.GONE);
    }

    private class MediaRouterCallback extends MediaRouter.Callback {
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
        mSelectedDevice = castDevice;
        mRouteStateListener = mediaRouteStateChangeListener;

        String deviceName = castDevice.getFriendlyName();
        Toast.makeText(this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();

        openSession();
    }

    /**
     * Starts a new video playback session with the current CastContext and selected device.
     */
    private void openSession() {
        mSession = new ApplicationSession(mCastContext, mSelectedDevice);

        // TODO: The below lines allow you to specify either that your application uses the default
        // implementations of the Notification and Lock Screens, or that you will be using your own.
        int flags = 0;

        mSession.setApplicationOptions(flags);

        mSession.setListener(new com.google.cast.ApplicationSession.Listener() {

            @Override
            public void onSessionStarted(ApplicationMetadata appMetadata) {

                Toast.makeText(mActivity, "Session Started.", Toast.LENGTH_SHORT).show();

                ApplicationChannel channel = mSession.getChannel();

                if (channel == null) return;

                mMediaMessageStream = new MediaProtocolMessageStream();
                channel.attachMessageStream(mMediaMessageStream);

                mCastKeysMessageStream = new CastKeysMessageStream();
                channel.attachMessageStream(mCastKeysMessageStream);

                mCastKeysMessageStream.changeTheme(mTheme);
            }

            @Override
            public void onSessionStartFailed(SessionError sessionError) {
                Toast.makeText(mActivity, "Session Start Failed.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionEnded(SessionError error) {
                Toast.makeText(mActivity, "Session Ended.", Toast.LENGTH_LONG).show();
            }
        });

        try {
            Toast.makeText(this, "Starting Cast Keys Session", Toast.LENGTH_SHORT).show();
            mSession.startSession(mAppName);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to open session", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSetVolume(double volume) {
        try {
            if(mMediaMessageStream != null){                
                mMediaMessageStream.setVolume(volume);
                mRouteStateListener.onVolumeChanged(volume);    
            }            
        } catch (IllegalStateException e){
            Toast.makeText(mActivity, "Problem Setting Volume", Toast.LENGTH_SHORT).show();
        } catch (IOException e){
            Toast.makeText(mActivity, "Problem Setting Volume", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateVolume(double v) {

    }

    // Fragments

    public static class MainFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(POSITION_MAIN);
        }
    }

    public static class AboutFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_about, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(POSITION_ABOUT);
        }
    }

}
