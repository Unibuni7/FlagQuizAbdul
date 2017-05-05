package com.example.slmns.flagquizabdul;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // These are the keys for reading data from the SharedPreferences method.
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferencesChanged = true; // did preferences change?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // we set the default values in the app's SharedPreferences out of our Res/xml file.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // here we register listener for SharedPreferences changes.
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);


            setRequestedOrientation(
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

   // here we start the app.
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {
            // here we initialize the MainActivityFragment and start the quiz game.
            // the id for the fragment are in the content layout folder.
            MainActivityFragment Quizfragment = (MainActivityFragment)
                    getSupportFragmentManager().findFragmentById(
                            R.id.quizfragment);
            Quizfragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            Quizfragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            Quizfragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    // show the menu if the app is running on phone or tablet.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //here we get the orientation of the device.
        int orientation = getResources().getConfiguration().orientation;

        // displays the menu on device
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // inflate the menu
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else
            return false;
    }

    // displays the settings activity.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    // listener for changes in the settings
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true; // user changed app setting

                    MainActivityFragment Quizfragment = (MainActivityFragment)
                            getSupportFragmentManager().findFragmentById(
                                    R.id.quizfragment);

                    if (key.equals(CHOICES)) { // # of choices to display changed
                        Quizfragment.updateGuessRows(sharedPreferences);
                        Quizfragment.resetQuiz();
                    }
                    else if (key.equals(REGIONS)) { // regions to include changed
                        Set<String> regions =
                                sharedPreferences.getStringSet(REGIONS, null);

                        if (regions != null && regions.size() > 0) {
                            Quizfragment.updateRegions(sharedPreferences);
                            Quizfragment.resetQuiz();
                        }
                        else {
                            // must select one region--set Europe as default
                            SharedPreferences.Editor editor =
                                    sharedPreferences.edit();
                            regions.add(getString(R.string.default_region));
                            editor.putStringSet(REGIONS, regions);
                            editor.apply();

                            Toast.makeText(MainActivity.this,
                                    R.string.default_region_message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    Toast.makeText(MainActivity.this,
                            R.string.restarting_quiz,
                            Toast.LENGTH_SHORT).show();
                }
            };
}
