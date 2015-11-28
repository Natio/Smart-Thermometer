package com.michead.smarterthermometer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Simone on 11/28/2015.
 */
public class SettingsScreen extends Fragment {

    private TextView locationTW;
    private Button submitB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_screen, container, false);

        locationTW = (TextView)rootView.findViewById(R.id.location);
        submitB = (Button)rootView.findViewById(R.id.submit);

        return rootView;
    }

}
