package com.github.trainjezelf.datastore;

import android.content.Context;

import com.github.trainjezelf.R;

/**
 * Data class that holds the settings for one reminder
 */
public class Reminder {

    public static final int NEW_REMINDER_UID = -1;

    /**
     * Default values for a new reminder
     */
    public static final String DEFAULT_REMINDER_MESSAGE = "";
    public static final int DEFAULT_NUMBER_OF_NOTIFIES_PER_PERIOD = 5;
    public static final Period DEFAULT_PERIOD = Period.DAILY;

    /**
     * Period enum for defining the frequency of the reminder
     */
    public enum Period {
        HOURLY (R.string.hour, "buttonHour"),
        DAILY (R.string.day,"buttonDay"),
        WEEKLY (R.string.week, "buttonWeek"),
        MONTHLY (R.string.month, "buttonMonth");

        private final int nameResource;
        private final String buttonId;

        Period(int nameResource, String buttonId) {
            this.nameResource = nameResource;
            this.buttonId = buttonId;
        }

        public String toString(Context context) {
            return context.getResources().getString(nameResource);
        }

        public String getButtonId() { return buttonId; }

        public static Period get(Context context, String id) {
            for (Period candidate : Period.values()) {
                if (id.equals(candidate.toString(context))) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException(id + " not found");
        }
    }

    /**
     * Android context
     * transient: exclude from GSON serialization/deserialization
     */
    private transient Context context;

    /**
     * The reminder message
     */
    private final String message;

    /**
     * The reminder frequency
     */
    private final int numberOfNotifiesPerPeriod;
    private final Period period;

    /**
     * The reminder unique ID
     */
    private final int uniqueId;

    /**
     * Number of notifications that occurred before the user cancelled them
     */
    private int numberOfNotifications;

    /**
     * Constructor
     * @param message message next
     * @param numberOfNotifiesPerPeriod number of notifies per period
     * @param period period
     */
    public Reminder(String message, int numberOfNotifiesPerPeriod, Period period, int uniqueId) {
        this.message = message;
        this.numberOfNotifiesPerPeriod = numberOfNotifiesPerPeriod;
        this.period = period;
        this.uniqueId = uniqueId;
        this.numberOfNotifications = 0;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getMessage() {
        return message;
    }

    public int getNumberOfNotifiesPerPeriod() {
        return numberOfNotifiesPerPeriod;
    }

    public Period getPeriod() {
        return period;
    }

    public String frequencyToString() {
        return String.format("%d %s %s", numberOfNotifiesPerPeriod,
                context.getResources().getString(R.string.times_per), period.toString(context).toLowerCase());
    }

    public int getUniqueId() { return uniqueId; }

    public int getNumberOfNotifications() {
        return numberOfNotifications;
    }

    public void setNumberOfNotifications(int number) {
        numberOfNotifications = number;
    }
}
