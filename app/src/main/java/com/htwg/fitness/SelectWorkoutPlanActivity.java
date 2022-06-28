package com.htwg.fitness;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.htwg.fitness.MainActivity.INTENT_KEY_MESSAGE;
import static com.htwg.fitness.SettingsActivity.PREFS_NAME;
import static com.htwg.fitness.SettingsActivity.PREF_DARK_THEME;
import static com.htwg.fitness.SettingsActivity.PREF_ENGLISH;

public class SelectWorkoutPlanActivity extends AppCompatActivity {

    private String globalMessage;
    private ArrayList<String> arrayList;

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
        setContentView(R.layout.activity_select_workout_plan);
        setTitle(getResources().getString(R.string.select_workout_plan));

        Intent intent = getIntent();
        final String message = intent.getStringExtra(MainActivity.INTENT_KEY_MESSAGE);
        globalMessage = message.substring(0, 1);

        ListView listView = (ListView) findViewById(R.id.list_view);

        prepareListData(message);

        final ListViewAdapter adapter = new ListViewAdapter(this, arrayList);

        listView.setAdapter(adapter);

        findViewById(R.id.show_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!adapter.getSelectedItem().isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), SelectedWorkoutPlanActivity.class);
                    intent.putExtra(INTENT_KEY_MESSAGE, message +
                            adapter.getSelectedPosition() + adapter.getSelectedItem());
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.select_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!adapter.getSelectedItem().isEmpty()) {
                    saveSelectedWorkoutPlan(message, adapter.getSelectedPosition());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), DifficultyLevelActivity.class);
        intent.putExtra(INTENT_KEY_MESSAGE, globalMessage);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void prepareListData(String message) {
        arrayList = new ArrayList<>();
        String json;

        try {
            InputStream inputStream;

            if (getResources().getConfiguration().locale.toString().equals("de_DE")
                    || getResources().getConfiguration().locale.toString().equals("de")) {
                inputStream = this.getAssets().open("workout_plans.json");
            } else {
                inputStream = this.getAssets().open("workout_plans_en.json");
            }

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray workoutPlans = jsonObject.getJSONArray("workout_plans");

            switch (message) {
                case "11":
                    for (int i = 0; i < 3; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "12":
                    for (int i = 3; i < 6; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "13":
                    for (int i = 6; i < 9; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "21":
                    for (int i = 9; i < 12; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "22":
                    for (int i = 12; i < 15; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "23":
                    for (int i = 15; i < 18; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "31":
                    for (int i = 18; i < 20; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "32":
                    for (int i = 20; i < 22; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
                case "33":
                    for (int i = 22; i < 24; i++) {
                        JSONObject item = workoutPlans.getJSONObject(i);
                        arrayList.add(item.get("title").toString());
                    }
                    break;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveSelectedWorkoutPlan(String message, int selectedPosition) {
        String fileName = "saved_workout_plan";
        String fileContents;
        FileOutputStream outputStream;
        String json;

        try {
            InputStream inputStream;

            if (getResources().getConfiguration().locale.toString().equals("de_DE")
                    || getResources().getConfiguration().locale.toString().equals("de")) {
                inputStream = this.getAssets().open("workout_plans.json");
            } else {
                inputStream = this.getAssets().open("workout_plans_en.json");
            }

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray workoutPlans = jsonObject.getJSONArray("workout_plans");
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            switch (message) {
                case "11":
                    fileContents = workoutPlans.getJSONObject(selectedPosition).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "12":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 3).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "13":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 6).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "21":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 9).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "22":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 12).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "23":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 15).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "31":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 18).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "32":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 20).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
                case "33":
                    fileContents = workoutPlans.getJSONObject(selectedPosition + 22).toString();
                    outputStream.write(fileContents.getBytes());
                    outputStream.close();
                    break;
            }

            Toast.makeText(getApplicationContext(), getString(R.string.saving_workout_plan_successful), Toast.LENGTH_LONG).show();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
