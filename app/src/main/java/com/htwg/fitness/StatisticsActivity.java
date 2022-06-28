package com.htwg.fitness;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.htwg.fitness.MainActivity.STEPS;
import static com.htwg.fitness.SettingsActivity.PREFS_NAME;
import static com.htwg.fitness.SettingsActivity.PREF_DARK_THEME;
import static com.htwg.fitness.SettingsActivity.PREF_ENGLISH;

public class StatisticsActivity extends AppCompatActivity {

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

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
        setContentView(R.layout.activity_statistics);
        setTitle(getResources().getString(R.string.statistics));

        TextView textView = (TextView) findViewById(R.id.statistics_not_available);
        TextView statistic1 = (TextView) findViewById(R.id.statistic_1);
        TextView statistic2 = (TextView) findViewById(R.id.statistic_2);
        TextView statistic3 = (TextView) findViewById(R.id.statistic_3);
        TextView statistic4 = (TextView) findViewById(R.id.statistic_4);
        TextView statistic5 = (TextView) findViewById(R.id.statistic_5);
        TextView statistic6 = (TextView) findViewById(R.id.statistic_6);

        boolean statisticsAvailable = false;
        int numberOfWorkoutDays = 0;
        List<String> workoutDays = new ArrayList<>();
        List<Date> dates = new ArrayList<>();

        for (int i = 0; i < fileList().length; i++) {
            if (fileList()[i].startsWith("20")) {
                statisticsAvailable = true;
                numberOfWorkoutDays++;
                workoutDays.add(fileList()[i]);

                try {
                    Date date = format.parse(fileList()[i]);
                    dates.add(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!statisticsAvailable) {
            textView.setText(getString(R.string.statistics_not_available_yet));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);

            String numberOfWorkoutDaysText = getString(R.string.number_of_workout_days) + " " + numberOfWorkoutDays;
            statistic2.setText(numberOfWorkoutDaysText);

            int numberOfExercises = 0;
            List<String> exercises = new ArrayList<>();

            for (int i = 0; i < numberOfWorkoutDays; i++) {
                String fileName = workoutDays.get(i);
                StringBuilder sb = new StringBuilder();

                try {
                    FileInputStream inputStream = openFileInput(fileName);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String current;
                    while ((current = reader.readLine()) != null) {
                        sb.append(current);
                    }

                    reader.close();

                    String data = sb.toString();
                    String[] values = data.split(",");
                    numberOfExercises = numberOfExercises + values.length;
                    exercises.addAll(Arrays.asList(values));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String numberOfExercisesText = getString(R.string.number_of_exercises) + " " + numberOfExercises;
            statistic3.setText(numberOfExercisesText);

            String mostCommonExercise = mostCommon(exercises);
            String mostCommonExerciseText = getString(R.string.most_common_exercise) + " " + mostCommonExercise;
            statistic5.setText(mostCommonExerciseText);

            String numberOfExercisesPerDay = getString(R.string.number_of_exercises_per_day) + " " + (numberOfExercises / numberOfWorkoutDays);
            statistic4.setText(numberOfExercisesPerDay);

            Date minDate = Collections.min(dates);
            String workoutStart = getString(R.string.workout_start) + " " + formatter.format(minDate);
            statistic1.setText(workoutStart);

            int numberOfSteps = preferences.getInt(STEPS, 0);
            String steps = getString(R.string.steps) + " " + numberOfSteps;
            statistic6.setText(steps);
        }

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

    private <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue()) {
                max = e;
            }
        }

        return max.getKey();
    }

}
