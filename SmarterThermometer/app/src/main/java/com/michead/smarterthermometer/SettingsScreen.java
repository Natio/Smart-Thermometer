package com.michead.smarterthermometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Simone on 11/28/2015.
 */
public class SettingsScreen extends Fragment implements Button.OnClickListener {

    private TextView tv;
    private EditText locationET;
    private Button submitB;
    private Resources res;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_screen, container, false);

        res = getResources();

        tv = (TextView)rootView.findViewById(R.id.loc_title);
        tv.setText(res.getStringArray(R.array.settings)[3]);

        locationET = (EditText)rootView.findViewById(R.id.location);
        locationET.setHint(res.getStringArray(R.array.settings)[2]);

        submitB = (Button)rootView.findViewById(R.id.submit);
        submitB.setOnClickListener(this);
        submitB.setText("Save");
        submitB.requestFocus();

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        locationET.setText(Utils.everyWordToUpperCase(Utils.getLocation(getActivity())));
    }


    @Override
    public void onClick(View v) {

        String location = locationET.getText() + "";

        String saveToastText = res.getStringArray(R.array.settings)[0];
        String invalidLocText = res.getStringArray(R.array.settings)[1];

        if (location.equals(""))
            Toast.makeText(getActivity(), invalidLocText, Toast.LENGTH_SHORT).show();
        else{
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Utils.LOCATION_KEY, location.toLowerCase());
            editor.commit();
        }

        locationET.setText(Utils.everyWordToUpperCase(location));
        Toast.makeText(getActivity(), saveToastText + Utils.TEXTVIEW_SEPARATOR + locationET.getText(), Toast.LENGTH_SHORT).show();

        try{
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        catch(NullPointerException npe){}
    }
}
