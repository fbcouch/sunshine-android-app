package com.jamicouch.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jamicouch.sunshine.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ForecastDetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    private String mForecastUri;
    private String mForecastData;
    private TextView mDetailText;

    ShareActionProvider mShareActionProvider;

    public ForecastDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast_detail, container, false);

        mDetailText = (TextView)rootView.findViewById(R.id.forecast_detail_textview);
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mForecastUri = intent.getDataString();
        }
//        detailText.setText(mForecastUri);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastdetailfragment, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);

        mShareActionProvider = new ShareActionProvider(getActivity());
        MenuItemCompat.setActionProvider(shareItem, mShareActionProvider);

        if (mShareActionProvider != null && mForecastData != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, mForecastData + " " + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Uri.parse(mForecastUri), FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst())
            return;
        double high = data.getDouble(COL_WEATHER_MAX_TEMP);
        double low = data.getDouble(COL_WEATHER_MIN_TEMP);
        boolean isMetric = Utility.isMetric(getActivity());
        String highAndLow = Utility.formatTemperature(getActivity(), high, isMetric) + "/" + Utility.formatTemperature(getActivity(), low, isMetric);

        mForecastData = Utility.formatDate(data.getLong(COL_WEATHER_DATE)) +
                " - " + data.getString(COL_WEATHER_DESC) +
                " - " + highAndLow;

        mDetailText.setText(mForecastData);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
