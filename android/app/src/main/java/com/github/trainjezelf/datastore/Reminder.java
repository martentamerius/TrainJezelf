package com.github.trainjezelf.datastore;

/**
 * Data class that holds the settings for one reminder
 */
public class Reminder {

    public static final int NEW_REMINDER_UID = -1;

    /**
     * Period enum for defining the frequency of the reminder
     */
    public enum Period {
        HOURLY ("uur", "buttonHour"),
        DAILY ("dag","buttonDay"),
        WEEKLY ("week", "buttonWeek"),
        MONTHLY ("maand", "buttonMonth");

        private final String name;
        private final String buttonId;

        Period(String name, String buttonId) {
            this.name = name;
            this.buttonId = buttonId;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getButtonId() { return buttonId; }

        public static Period get(String id) {
            final String lowerCase = id.toLowerCase();
            for (Period candidate : Period.values()) {
                if (lowerCase.equals(candidate.toString())) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException(id + " not found");
        }
    }

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
        return String.format("%d keer per %s", numberOfNotifiesPerPeriod, period);
    }

    public int getUniqueId() { return uniqueId; }
}
