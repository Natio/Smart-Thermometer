package com.michead.smarterthermometer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Simone on 11/28/2015.
 */
public class SettingsScreen extends Fragment implements Button.OnClickListener {

    private TextView tv;
    private EditText locationTW;
    private Button submitB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_screen, container, false);

        tv = (TextView)rootView.findViewById(R.id.loc_title);
        tv.setText("Sensor location");

        locationTW = (EditText)rootView.findViewById(R.id.location);
        locationTW.setHint("Enter your sensor location");
        locationTW.setText(Utils.getLocation(getActivity()));

        submitB = (Button)rootView.findViewById(R.id.submit);
        submitB.setOnClickListener(this);
        submitB.setText("Save");
        submitB.requestFocus();

        return rootView;
    }


    @Override
    public void onClick(View v) {

        String location = locationTW.getText() + "";

        if (location.equals(""))
            Toast.makeText(getActivity(), "Invalid location", Toast.LENGTH_SHORT).show();
        else{
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Utils.LOCATION_KEY, location);
            editor.commit();
        }

        submitB.requestFocus();
    }
}
