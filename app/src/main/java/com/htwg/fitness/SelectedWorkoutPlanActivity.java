package com.htwg.fitness;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class SelectedWorkoutPlanActivity extends AppCompatActivity {

    private String globalMessage;
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
        setContentView(R.layout.activity_selected_workout_plan);

        Intent intent = getIntent();
        final String message = intent.getStringExtra(MainActivity.INTENT_KEY_MESSAGE);
        globalMessage = message.substring(0, 3);
        int index = message.length();
        final String title = message.substring(3, index);
        setTitle(title);

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.list_exp_selected);

        prepareListData();

        ExpandableListAdapter adapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        listView.setAdapter(adapter);

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final Dialog dialog;

                if (useDarkTheme) {
                    dialog = new Dialog(SelectedWorkoutPlanActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                } else {
                    dialog = new Dialog(SelectedWorkoutPlanActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                }

                dialog.setContentView(R.layout.exercise);

                TextView textView = (TextView) dialog.findViewById(R.id.exercise_desc);
                String json;

                try {
                    InputStream inputStream;

                    if (getResources().getConfiguration().locale.toString().equals("de_DE")
                            || getResources().getConfiguration().locale.toString().equals("de")) {
                        inputStream = getAssets().open("exercises.json");
                    } else {
                        inputStream = getAssets().open("exercises_en.json");
                    }

                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    inputStream.close();
                    json = new String(buffer, "UTF-8");
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray exercises = jsonObject.getJSONArray("exercises");

                    for (int i = 0; i < exercises.length(); i++) {
                        JSONObject current = exercises.getJSONObject(i);
                        String name = current.get("name").toString();

                        if (name.equals(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition))) {
                            WebView webView = (WebView) dialog.findViewById(R.id.webview);
                            webView.setWebViewClient(new WebViewClient() {
                                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                    Toast.makeText(SelectedWorkoutPlanActivity.this, description, Toast.LENGTH_SHORT).show();
                                }
                            });

                            WebSettings webSettings = webView.getSettings();
                            webSettings.setJavaScriptEnabled(true);

                            String description = current.get("description").toString();
                            String url = current.get("url").toString();

                            if (isNetworkAvailable()) {
                                webView.setVisibility(View.VISIBLE);
                                webView.loadUrl(url);
                            } else {
                                webView.setVisibility(View.INVISIBLE);
                                textView.setText(description);
                            }
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                Button dialogButton = (Button) dialog.findViewById(R.id.exercise_button);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return false;
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), SelectWorkoutPlanActivity.class);
        intent.putExtra(INTENT_KEY_MESSAGE, globalMessage.substring(0, 2));
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
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

            switch (globalMessage) {
                case "110": {
                    JSONObject item = workoutPlans.getJSONObject(0);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "111": {
                    JSONObject item = workoutPlans.getJSONObject(1);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "112": {
                    JSONObject item = workoutPlans.getJSONObject(2);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "120": {
                    JSONObject item = workoutPlans.getJSONObject(3);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "121": {
                    JSONObject item = workoutPlans.getJSONObject(4);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "122": {
                    JSONObject item = workoutPlans.getJSONObject(5);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "130": {
                    JSONObject item = workoutPlans.getJSONObject(6);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "131": {
                    JSONObject item = workoutPlans.getJSONObject(7);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "132": {
                    JSONObject item = workoutPlans.getJSONObject(8);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "210": {
                    JSONObject item = workoutPlans.getJSONObject(9);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "211": {
                    JSONObject item = workoutPlans.getJSONObject(10);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "212": {
                    JSONObject item = workoutPlans.getJSONObject(11);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "220": {
                    JSONObject item = workoutPlans.getJSONObject(12);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "221": {
                    JSONObject item = workoutPlans.getJSONObject(13);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "222": {
                    JSONObject item = workoutPlans.getJSONObject(14);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "230": {
                    JSONObject item = workoutPlans.getJSONObject(15);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "231": {
                    JSONObject item = workoutPlans.getJSONObject(16);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "232": {
                    JSONObject item = workoutPlans.getJSONObject(17);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "310": {
                    JSONObject item = workoutPlans.getJSONObject(18);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "311": {
                    JSONObject item = workoutPlans.getJSONObject(19);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "320": {
                    JSONObject item = workoutPlans.getJSONObject(20);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "321": {
                    JSONObject item = workoutPlans.getJSONObject(21);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "330": {
                    JSONObject item = workoutPlans.getJSONObject(22);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
                case "331": {
                    JSONObject item = workoutPlans.getJSONObject(23);
                    JSONArray days = item.getJSONArray("days");

                    for (int i = 0; i < days.length(); i++) {
                        JSONObject item2 = days.getJSONObject(i);
                        listDataHeader.add(item2.get("day").toString());

                        JSONArray exercises = item2.getJSONArray("exercises");
                        List<String> exerciseList = new ArrayList<>();

                        for (int j = 0; j < exercises.length(); j++) {
                            JSONObject item3 = exercises.getJSONObject(j);
                            exerciseList.add(item3.get("name").toString());
                        }

                        listDataChild.put(listDataHeader.get(i), exerciseList);
                    }
                    break;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}
