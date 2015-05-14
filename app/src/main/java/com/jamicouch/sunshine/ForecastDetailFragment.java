package com.jamicouch.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailFragment extends Fragment {

    public ForecastDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast_detail, container, false);

        TextView detailText = (TextView)rootView.findViewById(R.id.forecast_detail_textview);
        detailText.setText(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
        return rootView;
    }
}
