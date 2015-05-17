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
import android.widget.ImageView;
import android.widget.TextView;

import com.jamicouch.sunshine.data.WeatherContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ForecastDetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
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
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
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
    static final int COL_WEATHER_DEGREES = 9;
    static final int COL_WEATHER_WIND_SPEED = 10;
    static final int COL_WEATHER_HUMIDITY = 11;
    static final int COL_WEATHER_PRESSURE = 12;

    static final String ARG_FORECAST_URI = "forecastUri";

    private String mForecastUri;
    private String mForecastData;
    private ViewHolder mViewHolder;

    ShareActionProvider mShareActionProvider;

    public static ForecastDetailFragment newInstance(Uri dataUri) {
        ForecastDetailFragment forecastDetailFragment = new ForecastDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FORECAST_URI, dataUri.toString());
        forecastDetailFragment.setArguments(args);
        return forecastDetailFragment;
    }

    public ForecastDetailFragment() { setHasOptionsMenu(true); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mForecastUri = intent.getDataString();
        }

        Bundle arguments = getArguments();
        if (mForecastUri == null && arguments != null) {
            mForecastUri = arguments.getString(ARG_FORECAST_URI);
        }

        mViewHolder = new ViewHolder(rootView);
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

        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mForecastUri == null) {
            return null;
        }
        return new CursorLoader(getActivity(), Uri.parse(mForecastUri), DETAIL_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst())
            return;
        boolean isMetric = Utility.isMetric(getActivity());
        String dayName = Utility.getDayName(getActivity(), data.getLong(COL_WEATHER_DATE));
        mViewHolder.dayNameView.setText(dayName);

        String date = Utility.getFormattedMonthDay(getActivity(), data.getLong(COL_WEATHER_DATE));
        mViewHolder.dateView.setText(date);

        String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        mViewHolder.highView.setText(high);

        String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
        mViewHolder.lowView.setText(low);

        String forecast = data.getString(COL_WEATHER_DESC);
        mViewHolder.forecastView.setText(forecast);

        String humidity = Utility.getFormattedHumidity(getActivity(), data.getFloat(COL_WEATHER_HUMIDITY));
        mViewHolder.humidityView.setText(humidity);

        String wind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGREES));
        mViewHolder.windView.setText(wind);

        String pressure = Utility.getFormattedPressure(getActivity(), data.getFloat(COL_WEATHER_PRESSURE));
        mViewHolder.pressureView.setText(pressure);

        int image = Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID));
        mViewHolder.iconView.setImageResource(image);
        mViewHolder.iconView.setContentDescription(forecast);

        mForecastData = dayName + " " + date + " - " + forecast + " - " + high + " / " + low;

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = Uri.parse(mForecastUri);
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mForecastUri = updatedUri.toString();
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    static class ViewHolder {
        ImageView iconView;
        TextView dayNameView;
        TextView dateView;
        TextView highView;
        TextView lowView;
        TextView forecastView;
        TextView humidityView;
        TextView windView;
        TextView pressureView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.forecast_detail_icon);
            dayNameView = (TextView) view.findViewById(R.id.forecast_detail_day_name_textview);
            dateView = (TextView) view.findViewById(R.id.forecast_detail_date_textview);
            highView = (TextView) view.findViewById(R.id.forecast_detail_high_textview);
            lowView = (TextView) view.findViewById(R.id.forecast_detail_low_textview);
            forecastView = (TextView) view.findViewById(R.id.forecast_detail_forecast_textview);
            humidityView = (TextView) view.findViewById(R.id.forecast_detail_humidity_textview);
            windView = (TextView) view.findViewById(R.id.forecast_detail_wind_textview);
            pressureView = (TextView) view.findViewById(R.id.forecast_detail_pressure_textview);
        }
    }
}
