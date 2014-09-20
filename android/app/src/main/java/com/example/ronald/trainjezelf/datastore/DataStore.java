package com.example.ronald.trainjezelf.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * App-wide persistent data storage.
 * Created by Ronald on 4-7-2014.
 */
public final class DataStore {
    private final static String LOG_TAG = "DataStore";
    private final static String REMINDERS_KEY = "Reminders";
    private final static String LAST_NOTIFICATION_ID_KEY = "LastNotificationId";

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
    private Map<Integer, Reminder> reminders = null;

    /**
     * The last notification ID that our app used
     */
    private int lastNotificationId = 0;

    /**
     * Constructor
     *
     * @param context any context within the app
     */
    private DataStore(Context context) {
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        loadReminders();
        loadLastNotificationId();
    }

    /**
     * Get data store instance (singleton pattern)
     * @param context the activity
     * @return the data store
     */
    public static DataStore getInstance(Context context) {
        if (instance == null) {
            instance = new DataStore(context);
        }
        return instance;
    }

    /**
     * Get list of reminders
     * @return list of reminders
     */
    public List<Reminder> getReminders() {
        return new ArrayList<Reminder>(reminders.values());
    }

    /**
     * Add reminder to data store
     * @param reminder the reminder
     * @return new size of the reminder list
     */
    public int add(Reminder reminder) {
        reminders.put(reminder.getUniqueId(), reminder);
        saveReminders();
        return reminders.size();
    }

    /**
     * Remove oldReminder from data store
     * @param newReminder the new reminder that replaces the old one
     * @return the new Reminder
     */
    public Reminder replace(Reminder newReminder) {
        reminders.put(newReminder.getUniqueId(), newReminder);
        saveReminders();
        return newReminder;
    }

    /**
     * Remove item from data store
     * @param uniqueId the unique Id of the reminder
     * @return resulting list of reminders
     */
    public List<Reminder> removeReminder(int uniqueId) {
        reminders.remove(uniqueId);
        saveReminders();
        return getReminders();
    }

    /**
     * Get item from data store
     * @param uniqueId the unique Id of the reminder
     * @return reminder the reminder
     */
    public Reminder get(int uniqueId) {
        return reminders.get(uniqueId);
    }

    /**
     * Get next unique notification ID
     * @return unique notification ID
     */
    public int getNextNotificationId() {
        int result = lastNotificationId;
        lastNotificationId++;
        saveLastNotificationId();
        return result;
    }

    /**
     * Save reminders to data store
     */
    @SuppressLint("CommitPrefEdits")
    private void saveReminders() {
        final Gson gson = new Gson();
        final String json = gson.toJson(reminders);
        Log.d(LOG_TAG, "save reminders JSON: " + json);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REMINDERS_KEY, json);
        editor.commit();
    }

    /**
     * Get reminders from data store
     */
    private void loadReminders() {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<Map<Integer, Reminder>>() {}.getType();
        final String json = sharedPref.getString(REMINDERS_KEY, "[]");
        reminders = gson.fromJson(json, listType);
        Log.d(LOG_TAG, "loaded reminders: " + json + " size of map: " + reminders.size());
    }

    private void loadLastNotificationId() {
        lastNotificationId = sharedPref.getInt(LAST_NOTIFICATION_ID_KEY, 0);
    }

    private void saveLastNotificationId() {
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(LAST_NOTIFICATION_ID_KEY, lastNotificationId);
        editor.commit();
    }
}
