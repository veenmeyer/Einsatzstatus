package de.einsatzkomponente.einsatzstatus.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.einsatzkomponente.einsatzstatus.R;

/**
 * Created by Simon Wheeler on 06.01.2018.
 */

public class MyPreferenceActivity extends PreferenceActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}