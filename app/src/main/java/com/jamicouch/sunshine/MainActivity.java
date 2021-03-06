package com.jamicouch.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jamicouch.sunshine.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements ForecastFragment.Callback {
    private static final String TAG = MainActivity.class.getSimpleName();

    static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mLocation;
    private boolean mTwoPane;
    private boolean mNotificationPref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        mLocation = Utility.getPreferredLocation(this);
        mNotificationPref = Utility.getNotificationPreference(this);

        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new ForecastDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            } else {
                mTwoPane = false;
                getSupportActionBar().setElevation(0f);
            }
        }

        ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        forecastFragment.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        String currentLocation = Utility.getPreferredLocation(this);
        if (!mLocation.equals(currentLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            ff.onLocationChanged();

            ForecastDetailFragment fdf = (ForecastDetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (fdf != null) {
                fdf.onLocationChanged(currentLocation);
            }

            mLocation = currentLocation;
        }

        Boolean currentNotificationPref = Utility.getNotificationPreference(this);
        if (currentNotificationPref != mNotificationPref) {
            SunshineSyncAdapter.syncImmediately(this);

            mNotificationPref = currentNotificationPref;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, ForecastDetailFragment.newInstance(dateUri), DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, ForecastDetailActivity.class)
                    .setData(dateUri);
            startActivity(intent);
        }
    }
}
