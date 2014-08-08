package com.example.ronald.trainjezelf.datastore;

/**
 * Reminder class for holding the settings for one reminder
 */
public class Reminder {

    /**
     * Period enum for defining the frequency of the reminder
     */
    public enum Period {
        HOURLY ("uur"),
        DAILY ("dag"),
        WEEKLY ("week"),
        MONTHLY ("maand");

        private final String name;

        Period(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    /**
     * The reminder message
     */
    private String message;

    /**
     * The reminder frequency
     */
    private int numberOfNotifiesPerPeriod;
    private Period period;

    /**
     * Constructor
     * @param message
     * @param numberOfNotifiesPerPeriod
     * @param period
     */
    public Reminder(String message, int numberOfNotifiesPerPeriod, Period period) {
        this.message = message;
        this.numberOfNotifiesPerPeriod = numberOfNotifiesPerPeriod;
        this.period = period;
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

}
