package com.htwg.fitness;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.htwg.fitness.MainActivity.INTENT_KEY_MESSAGE;
import static com.htwg.fitness.SettingsActivity.PREFS_NAME;
import static com.htwg.fitness.SettingsActivity.PREF_DARK_THEME;
import static com.htwg.fitness.SettingsActivity.PREF_ENGLISH;

public class CreateWorkoutPlanActivity extends AppCompatActivity {

    private String message;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
        boolean useEnglish = preferences.getBoolean(PREF_ENGLISH, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }

        if (useEnglish) {
            setLocale();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout_plan);
        setTitle(getResources().getString(R.string.create_workout_plan));

        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.INTENT_KEY_MESSAGE);

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.list_exp_create);

        prepareListData();

        final ExpandableListWithCheckboxesAdapter adapter =
                new ExpandableListWithCheckboxesAdapter(this, listDataHeader, listDataChild, useDarkTheme);

        listView.setAdapter(adapter);

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!adapter.getSavedExercises().isEmpty()) {
                    List<Integer> selectedDays = new ArrayList<>();

                    for (int i = 0; i < adapter.getSavedExercises().size(); i++) {
                        String[] current = adapter.getSavedExercises().get(i).split(",");

                        if (!selectedDays.contains(Integer.parseInt(current[0]))) {
                            selectedDays.add(Integer.parseInt(current[0]));
                        }
                    }

                    if (selectedDays.size() < 6) {
                        createWorkoutPlan(adapter.getSavedExercises());
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        AlertDialog.Builder builder;

                        if (useDarkTheme) {
                            builder = new AlertDialog.Builder(CreateWorkoutPlanActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                        } else {
                            builder = new AlertDialog.Builder(CreateWorkoutPlanActivity.this);
                        }

                        builder.setMessage(getString(R.string.too_few_rest_days))
                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        createWorkoutPlan(adapter.getSavedExercises());
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
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
        Intent intent = new Intent(getApplicationContext(), SelectOrCreateActivity.class);
        intent.putExtra(INTENT_KEY_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.monday));
        listDataHeader.add(getString(R.string.tuesday));
        listDataHeader.add(getString(R.string.wednesday));
        listDataHeader.add(getString(R.string.thursday));
        listDataHeader.add(getString(R.string.friday));
        listDataHeader.add(getString(R.string.saturday));
        listDataHeader.add(getString(R.string.sunday));

        listDataChild = new HashMap<>();
        String json;

        try {
            InputStream inputStream;

            if (getResources().getConfiguration().locale.toString().equals("de_DE")
                    || getResources().getConfiguration().locale.toString().equals("de")) {
                inputStream = this.getAssets().open("exercises.json");
            } else {
                inputStream = this.getAssets().open("exercises_en.json");
            }

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray exercises = jsonObject.getJSONArray("exercises");

            for (int i = 0; i < 7; i++) {
                List<String> exerciseList = new ArrayList<>();

                for (int j = 0; j < exercises.length(); j++) {
                    JSONObject item = exercises.getJSONObject(j);
                    exerciseList.add(item.get("name").toString());
                }

                listDataChild.put(listDataHeader.get(i), exerciseList);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void createWorkoutPlan(List<String> savedExercises) {
        List<Integer> selectedDays = new ArrayList<>();

        for (int i = 0; i < savedExercises.size(); i++) {
            String[] current = savedExercises.get(i).split(",");

            if (!selectedDays.contains(Integer.parseInt(current[0]))) {
                selectedDays.add(Integer.parseInt(current[0]));
            }
        }

        String fileName = "saved_workout_plan";
        String fileContents;
        FileOutputStream outputStream;
        String json;

        try {
            InputStream inputStream;

            if (getResources().getConfiguration().locale.toString().equals("de_DE")
                    || getResources().getConfiguration().locale.toString().equals("de")) {
                inputStream = this.getAssets().open("exercises.json");
            } else {
                inputStream = this.getAssets().open("exercises_en.json");
            }

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray exercises = jsonObject.getJSONArray("exercises");

            JSONObject workoutPlan = new JSONObject();
            workoutPlan.put("title", getString(R.string.workout_plan));

            JSONArray days = new JSONArray();

            for (int i = 0; i < selectedDays.size(); i++) {
                JSONArray currentExercises = new JSONArray();

                for (int j = 0; j < savedExercises.size(); j++) {
                    String[] current = savedExercises.get(j).split(",");

                    if (Integer.parseInt(current[0]) == selectedDays.get(i)) {
                        JSONObject exercise = exercises.getJSONObject(Integer.parseInt(current[1]));
                        JSONObject object = new JSONObject();

                        object.put("name", exercise.get("name"));
                        currentExercises.put(object);
                    }
                }

                String day = "";

                switch (selectedDays.get(i)) {
                    case 0:
                        day = getString(R.string.monday);
                        break;
                    case 1:
                        day = getString(R.string.tuesday);
                        break;
                    case 2:
                        day = getString(R.string.wednesday);
                        break;
                    case 3:
                        day = getString(R.string.thursday);
                        break;
                    case 4:
                        day = getString(R.string.friday);
                        break;
                    case 5:
                        day = getString(R.string.saturday);
                        break;
                    case 6:
                        day = getString(R.string.sunday);
                        break;
                }

                JSONObject currentDay = new JSONObject();
                currentDay.put("day", day);
                currentDay.put("exercises", currentExercises);
                days.put(currentDay);
            }

            workoutPlan.put("days", days);

            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            fileContents = workoutPlan.toString();
            outputStream.write(fileContents.getBytes());
            outputStream.close();

            Toast.makeText(getApplicationContext(), getString(R.string.saving_workout_plan_successful), Toast.LENGTH_LONG).show();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
