package com.htwg.fitness;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpandableListWithCheckboxesAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private List<String> savedExercises;
    private boolean useDarkTheme;

    public ExpandableListWithCheckboxesAdapter(Context context, List<String> listDataHeader,
                                               HashMap<String, List<String>> listChildData,
                                               boolean useDarkTheme) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        this.savedExercises = new ArrayList<>();
        this.useDarkTheme = useDarkTheme;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.list_box, parent, false);

            viewHolder.label = (TextView) convertView.findViewById(R.id.list_box);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.label.setText(childText);

        if (savedExercises.contains(groupPosition + "," + childPosition)) {
            viewHolder.checkBox.setChecked(true);
        } else {
            viewHolder.checkBox.setChecked(false);
        }

        viewHolder.checkBox.setTag(getChild(groupPosition, childPosition));
        viewHolder.label.setTag(getChild(groupPosition, childPosition));

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!viewHolder.checkBox.isChecked()) {
                    savedExercises.remove(groupPosition + "," + childPosition);
                } else {
                    savedExercises.add(groupPosition + "," + childPosition);
                }
            }
        });

        viewHolder.label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog;

                if (useDarkTheme) {
                    dialog = new Dialog(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                } else {
                    dialog = new Dialog(context, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                }

                dialog.setContentView(R.layout.exercise);

                TextView textView = (TextView) dialog.findViewById(R.id.exercise_desc);
                String json;

                try {
                    InputStream inputStream;

                    if (context.getResources().getConfiguration().locale.toString().equals("de_DE")
                            || context.getResources().getConfiguration().locale.toString().equals("de")) {
                        inputStream = context.getAssets().open("exercises.json");
                    } else {
                        inputStream = context.getAssets().open("exercises_en.json");
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

                        if (name.equals(viewHolder.label.getText().toString())) {
                            WebView webView = (WebView) dialog.findViewById(R.id.webview);
                            webView.setWebViewClient(new WebViewClient() {
                                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                    Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
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
            }
        });

        return convertView;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView listGroup = (TextView) convertView.findViewById(R.id.list_group);
        listGroup.setTypeface(null, Typeface.BOLD);
        listGroup.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public List<String> getSavedExercises() {
        return savedExercises;
    }

    private class ViewHolder {
        private TextView label;
        private CheckBox checkBox;
    }

}
