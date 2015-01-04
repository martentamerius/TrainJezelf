package com.github.trainjezelf.datastore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides app data storage and retrieval via singleton pattern.
 */
public final class DataStore {
    private final static String REMINDERS_KEY = "Reminders";
    private final static String LAST_NOTIFICATION_ID_KEY = "LastNotificationId";

    /**
     * The singleton
     */
    private static DataStore instance = null;

    /**
     * The preferences object
     */
    private final SharedPreferences sharedPreferences;

    /**
     * The list of reminders
     */
    private Map<Integer, Reminder> reminders = null;

    /**
     * The last notification ID that our app used
     */
    private int lastNotificationId = 0;

    /**
     * Reference to Android app context
     */
    private final Context context;

    /**
     * Constructor
     * @param context the context
     */
    private DataStore(Context context) {
        this.context = context;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        loadReminders();
        loadLastNotificationId();
    }

    /**
     * Get data store instance (singleton pattern)
     * @param context the context
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
     * Put reminder in data store, possibly replacing an existing one with the same unique Id
     * @param reminder the reminder
     * @return the reminder
     */
    public Reminder put(Reminder reminder) {
        reminders.put(reminder.getUniqueId(), reminder);
        saveReminders();
        return reminder;
    }

    /**
     * Remove item from data store
     * @param uniqueId the unique Id of the reminder
     * @return updated list of reminders
     */
    public List<Reminder> removeReminder(int uniqueId) {
        reminders.remove(uniqueId);
        saveReminders();
        return getReminders();
    }

    /**
     * Get item from data store
     * @param uniqueId the unique Id of the reminder
     * @return the reminder
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
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(REMINDERS_KEY, json);
        editor.commit();
    }

    /**
     * Get reminders from data store
     * @return list of reminders
     */
    private void loadReminders() {
        final Gson gson = new Gson();
        final Type listType = new TypeToken<Map<Integer, Reminder>>() {}.getType();
        final String json = sharedPreferences.getString(REMINDERS_KEY, "[]");
        reminders = gson.fromJson(json, listType);
        for (Reminder reminder : reminders.values()) {
            reminder.setContext(context);
        }
    }

    /**
     * Get last notification ID from data store
     * @return last notification ID
     */
    private void loadLastNotificationId() {
        lastNotificationId = sharedPreferences.getInt(LAST_NOTIFICATION_ID_KEY, 0);
    }

    /**
     * Save last notification to data store
     */
    private void saveLastNotificationId() {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_NOTIFICATION_ID_KEY, lastNotificationId);
        editor.commit();
    }
}
