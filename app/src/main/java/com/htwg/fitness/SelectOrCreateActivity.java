package com.htwg.fitness;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import static com.htwg.fitness.MainActivity.INTENT_KEY_MESSAGE;
import static com.htwg.fitness.SettingsActivity.PREFS_NAME;
import static com.htwg.fitness.SettingsActivity.PREF_DARK_THEME;
import static com.htwg.fitness.SettingsActivity.PREF_ENGLISH;

public class SelectOrCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
        boolean useEnglish = preferences.getBoolean(PREF_ENGLISH, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }

        if (useEnglish) {
            setLocale();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_or_create);
        setTitle(getResources().getString(R.string.workout_plan));

        Intent intent = getIntent();
        final String message = intent.getStringExtra(MainActivity.INTENT_KEY_MESSAGE);

        findViewById(R.id.select_prebuilt_workout_plan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DifficultyLevelActivity.class);
                intent.putExtra(INTENT_KEY_MESSAGE, message);
                startActivity(intent);
            }
        });

        findViewById(R.id.create_individual_workout_plan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateWorkoutPlanActivity.class);
                intent.putExtra(INTENT_KEY_MESSAGE, message);
                startActivity(intent);
            }
        });

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setLocale() {
        Locale locale = new Locale("en");
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

}
