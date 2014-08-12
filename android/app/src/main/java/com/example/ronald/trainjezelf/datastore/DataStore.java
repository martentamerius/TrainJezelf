package com.example.ronald.trainjezelf.datastore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * App-wide persistent data storage.
 * Created by Ronald on 4-7-2014.
 */
public final class DataStore {
    private final static String REMINDERS_KEY = "Reminders";

    /**
     * The singleton
     */
    private static DataStore instance = null;

    /**
     * The preferences object
     */
    private final SharedPreferences sharedPref;

    /**
     * The list of reminders
     */
    private List<Reminder> reminders = null;

    /**
     * Constructor
     *
     * @param activity any context within the app
     */
    private DataStore(Activity activity) {
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        loadReminders();
    }

    /**
     * Get data store instance (singleton pattern)
     * @param activity the activity
     * @return the data store
     */
    public static DataStore getInstance(Activity activity) {
        if (instance == null) {
            instance = new DataStore(activity);
        }
        return instance;
    }

    /**
     * Get list of reminders
     * @return list of reminders
     */
    public List<Reminder> getReminders() {
        return reminders;
    }

    /**
     * Add reminder to data store
     * @param reminder the reminder
     * @return new size of the reminder list
     */
    public int add(Reminder reminder) {
        reminders.add(reminder);
        saveReminders();
        return reminders.size();
    }

    /**
     * Remove oldReminder from data store
     * @param oldReminder the oldReminder to replace
     * @param newReminder the new reminder that replaces the old one
     * @return the new Reminder
     */
    public Reminder replace(Reminder oldReminder, Reminder newReminder) {
        int replaceIndex = reminders.indexOf(oldReminder);
        reminders.set(replaceIndex, newReminder);
        saveReminders();
        return newReminder;
    }

    /**
     * Remove item from data store
     * @param index the index
     */
    public void remove(int index) {
        reminders.remove(index);
        saveReminders();
    }

    /**
     * Get item from data store
     * @param index the index
     * @return reminder the reminder
     */
    public Reminder get(int index) {
        return reminders.get(index);
    }

    /**
     * Save reminders to data store
     */
    @SuppressLint("CommitPrefEdits")
    private void saveReminders() {
        final Gson gson = new Gson();
        final String json = gson.toJson(reminders);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REMINDERS_KEY, json);
        editor.commit();
    }

    /**
     * Get reminders from data store
     */
    private void loadReminders() {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<Reminder>>() {}.getType();
        final String json = sharedPref.getString(REMINDERS_KEY, "[]");
        reminders = gson.fromJson(json, listType);
    }
}
