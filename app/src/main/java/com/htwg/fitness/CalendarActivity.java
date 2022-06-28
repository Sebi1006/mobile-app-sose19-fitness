package com.htwg.fitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

import static com.htwg.fitness.SettingsActivity.PREFS_NAME;
import static com.htwg.fitness.SettingsActivity.PREF_DARK_THEME;
import static com.htwg.fitness.SettingsActivity.PREF_ENGLISH;

@SuppressLint("RestrictedApi")
public class CalendarActivity extends AppCompatActivity {

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private String[] exerciseArray;
    private List<String> exercises;

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
        setContentView(R.layout.activity_calendar);
        setTitle(getResources().getString(R.string.calendar));

        prepareExerciseList();

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR, -20);

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 20);

        final HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendar_view)
                .range(startDate, endDate)
                .datesNumberOnScreen(7)
                .build();

        final Calendar calendar = Calendar.getInstance();

        displayContent(calendar, useDarkTheme);

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(final Calendar date, int position) {
                displayContent(date, useDarkTheme);
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

    private void prepareExerciseList() {
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
            exerciseArray = new String[exercises.length()];

            for (int i = 0; i < exercises.length(); i++) {
                JSONObject item = exercises.getJSONObject(i);
                exerciseArray[i] = item.get("name").toString();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayContent(final Calendar calendar, final boolean useDarkTheme) {
        if (Arrays.asList(fileList()).contains(formatter.format(calendar.getTime()))) {
            FloatingActionButton addButton = findViewById(R.id.fab_add);
            addButton.setVisibility(View.INVISIBLE);
            StringBuilder sb = new StringBuilder();

            try {
                FileInputStream inputStream = openFileInput(formatter.format(calendar.getTime()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String current;
                while ((current = reader.readLine()) != null) {
                    sb.append(current);
                }

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String data = sb.toString();
            String[] values = data.split(",");

            final ArrayList<String> loadedExercises = new ArrayList<>();
            loadedExercises.addAll(Arrays.asList(values));

            ListView listView = (ListView) findViewById(R.id.list_exercises);
            listView.setVisibility(View.VISIBLE);

            CalendarListAdapter adapter = new CalendarListAdapter(this, loadedExercises);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Dialog dialog;

                    if (useDarkTheme) {
                        dialog = new Dialog(CalendarActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    } else {
                        dialog = new Dialog(CalendarActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
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

                            if (name.equals(loadedExercises.get(position))) {
                                WebView webView = (WebView) dialog.findViewById(R.id.webview);
                                webView.setWebViewClient(new WebViewClient() {
                                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                        Toast.makeText(CalendarActivity.this, description, Toast.LENGTH_SHORT).show();
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
                }
            });

            FloatingActionButton deleteButton = findViewById(R.id.fab_delete);
            deleteButton.setVisibility(View.VISIBLE);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder;

                    if (useDarkTheme) {
                        builder = new AlertDialog.Builder(CalendarActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                    } else {
                        builder = new AlertDialog.Builder(CalendarActivity.this);
                    }

                    builder.setMessage(getString(R.string.delete_exercises))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    File dir = getFilesDir();
                                    File file = new File(dir, formatter.format(calendar.getTime()));
                                    file.delete();

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(), getString(R.string.deleting_exercises_successful), Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } else {
            ListView listView = (ListView) findViewById(R.id.list_exercises);
            listView.setVisibility(View.INVISIBLE);

            FloatingActionButton deleteButton = findViewById(R.id.fab_delete);
            deleteButton.setVisibility(View.INVISIBLE);

            exercises = new ArrayList<>();

            FloatingActionButton addButton = findViewById(R.id.fab_add);
            addButton.setVisibility(View.VISIBLE);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!Arrays.asList(fileList()).contains("saved_workout_plan")) {
                        builder(calendar, useDarkTheme);
                    } else {
                        prepareListData();

                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        final String weekDay = getDayOfWeek(dayOfWeek);
                        boolean auto = false;

                        for (int i = 0; i < listDataHeader.size(); i++) {
                            if (listDataHeader.get(i).equals(weekDay)) {
                                auto = true;
                            }
                        }

                        if (auto) {
                            AlertDialog.Builder builder;

                            if (useDarkTheme) {
                                builder = new AlertDialog.Builder(CalendarActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                            } else {
                                builder = new AlertDialog.Builder(CalendarActivity.this);
                            }

                            builder.setMessage(getString(R.string.auto_enter_exercises))
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            List<String> autoExercises = listDataChild.get(weekDay);

                                            StringBuilder sb = new StringBuilder();
                                            String fileName = formatter.format(calendar.getTime());
                                            String fileContents;
                                            FileOutputStream outputStream;

                                            for (int i = 0; i < autoExercises.size(); i++) {
                                                sb.append(autoExercises.get(i)).append(",");
                                            }

                                            fileContents = sb.toString();

                                            try {
                                                outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                                                outputStream.write(fileContents.getBytes());
                                                outputStream.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            Toast.makeText(getApplicationContext(), getString(R.string.entering_exercises_successful), Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            builder(calendar, useDarkTheme);
                                        }
                                    });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            builder(calendar, useDarkTheme);
                        }
                    }
                }
            });
        }
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

    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return getString(R.string.sunday);
            case 2:
                return getString(R.string.monday);
            case 3:
                return getString(R.string.tuesday);
            case 4:
                return getString(R.string.wednesday);
            case 5:
                return getString(R.string.thursday);
            case 6:
                return getString(R.string.friday);
            case 7:
                return getString(R.string.saturday);
        }

        return null;
    }

    private void builder(final Calendar calendar, final boolean useDarkTheme) {
        AlertDialog.Builder builder;

        if (useDarkTheme) {
            builder = new AlertDialog.Builder(CalendarActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        } else {
            builder = new AlertDialog.Builder(CalendarActivity.this);
        }

        builder.setTitle(getString(R.string.enter_exercises));

        builder.setMultiChoiceItems(exerciseArray, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    exercises.add(exerciseArray[which]);
                } else {
                    exercises.remove(exerciseArray[which]);
                }
            }
        });

        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!exercises.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    String fileName = formatter.format(calendar.getTime());
                    String fileContents;
                    FileOutputStream outputStream;

                    for (int i = 0; i < exercises.size(); i++) {
                        sb.append(exercises.get(i)).append(",");
                    }

                    fileContents = sb.toString();

                    try {
                        outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
                        outputStream.write(fileContents.getBytes());
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), getString(R.string.entering_exercises_successful), Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exercises.clear();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
