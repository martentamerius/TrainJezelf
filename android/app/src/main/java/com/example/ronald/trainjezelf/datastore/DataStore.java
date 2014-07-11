package com.example.ronald.trainjezelf.datastore;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
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
     * Dirty flag: true means we have changes w.r.t. the contents on permanent storage
     */
    private boolean isDirty;

    /**
     * Constructor
     *
     * @param activity any context within the app
     */
    protected DataStore(Activity activity) {
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        this.reminders = loadReminders();
        this.isDirty = false;
    }

    /**
     * Get data store instance (singleton pattern)
     * @param activity
     * @return the data store
     */
    public static DataStore getInstance(Activity activity) {
        if (instance == null) {
            instance = new DataStore(activity);
        }
        return instance;
    }

    /**
     * Save reminders to disk (if needed)
     */
    public void saveState() {
        if (isDirty) {
            saveReminders(reminders);
            isDirty = false;
        }
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
     * @param reminder
     * @return new size of the reminder list
     */
    public int add(Reminder reminder) {
        this.reminders.add(reminder);
        isDirty = true;
        return reminders.size();
    }

    /**
     * Remove item from data store
     * @param item
     */
    public void remove(Reminder item) {
        reminders.remove(item);
    }

    /**
     * Remove item from data store
     * @param index index
     */
    public void remove(int index) {
        reminders.remove(index);
    }

    /**
     * Get item from data store
     * @param index
     * @return reminder
     */
    public Reminder get(int index) {
        return reminders.get(index);
    }

    /**
     * Save reminders to data store
     * @param reminders
     */
    private void saveReminders(List<Reminder> reminders) {
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
    private List<Reminder> loadReminders() {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<List<Reminder>>() {}.getType();
        final String json = sharedPref.getString(REMINDERS_KEY, "[]");
        return gson.fromJson(json, listType);
    }
}
