package com.example.ronald.trainjezelf.datastore;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Ronald on 4-7-2014.
 */
public class DataStore {
    private final static String REMINDERS_KEY = "Reminders";

    /**
     * Activity for which to store data
     */
    private final Activity activity;

    /**
     * The preferences object
     */
    private final SharedPreferences sharedPref;

    /**
     * Constructor
     *
     * @param activity for which to store data
     */
    public DataStore(Activity activity) {
        this.activity = activity;
        this.sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
    }

    /**
     * Save reminders to data store
     * @param reminders
     */
    public void saveReminders(List<Reminder> reminders) {
        final Gson gson = new Gson();
        final String json = gson.toJson(reminders);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REMINDERS_KEY, json);
        editor.commit();
    }

    /**
     * Get reminders from data store
     * @return list of reminders
     */
    public List<Reminder> loadReminders() {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<Reminder>>() {}.getType();
        final String json = sharedPref.getString(REMINDERS_KEY, "[]");
        return gson.fromJson(json, listType);
    }
}
