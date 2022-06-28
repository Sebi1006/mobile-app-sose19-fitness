package com.htwg.fitness;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.htwg.fitness.SettingsActivity.PREFS_NAME;
import static com.htwg.fitness.SettingsActivity.PREF_DARK_THEME;
import static com.htwg.fitness.SettingsActivity.PREF_ENGLISH;

public class WorkoutPlanActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_workout_plan);

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.list_exp_wp);

        prepareListData();

        ExpandableListAdapter adapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        listView.setAdapter(adapter);

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final Dialog dialog;

                if (useDarkTheme) {
                    dialog = new Dialog(WorkoutPlanActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                } else {
                    dialog = new Dialog(WorkoutPlanActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                }

                dialog.setContentView(R.layout.exercise);

                TextView textView = (TextView) dialog.findViewById(R.id.exercise_desc);
                String json;
                boolean available = false;

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
                                    Toast.makeText(WorkoutPlanActivity.this, description, Toast.LENGTH_SHORT).show();
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

                            available = true;
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

                if (available) {
                    dialog.show();
                }

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

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String json;

        if (!Arrays.asList(fileList()).contains("saved_workout_plan")) {
            TextView textView = (TextView) findViewById(R.id.workout_plan_not_available);
            textView.setText(getString(R.string.first_create_workout_plan));
        } else {
            try {
                FileInputStream inputStream = openFileInput("saved_workout_plan");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String current;
                while ((current = reader.readLine()) != null) {
                    sb.append(current);
                }

                reader.close();
                json = sb.toString();

                JSONObject jsonObject = new JSONObject(json);
                JSONArray days = jsonObject.getJSONArray("days");

                setTitle(jsonObject.get("title").toString());

                for (int i = 0; i < days.length(); i++) {
                    JSONObject day = days.getJSONObject(i);
                    listDataHeader.add(day.get("day").toString());

                    JSONArray exercises = day.getJSONArray("exercises");
                    List<String> exerciseList = new ArrayList<>();

                    for (int j = 0; j < exercises.length(); j++) {
                        JSONObject name = exercises.getJSONObject(j);
                        exerciseList.add(name.get("name").toString());
                    }

                    listDataChild.put(listDataHeader.get(i), exerciseList);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
