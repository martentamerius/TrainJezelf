package com.github.trainjezelf.datastore;

/**
 * Data class that holds a time range hh:mm -- hh:mm
 */
public class TimeRange {

    private int fromHour;
    private int fromMinute;

    private int untilHour;
    private int untilMinute;

    /**
     * Construct new TimeRange object from String containing the encoded version
     * @param encoded the encoded TimeRange
     */
    public TimeRange(String encoded) {
        final String[] times = encoded.split("-");
        final String[] fromPieces = times[0].split(":");
        final String[] untilPieces = times[1].split(":");
        fromHour = decodeInteger(fromPieces[0], 0, 23);
        fromMinute = decodeInteger(fromPieces[1], 0, 59);
        untilHour = decodeInteger(untilPieces[0], 0, 23);
        untilMinute = decodeInteger(untilPieces[1], 0, 59);
    }

    /**
     * Copy constructor
     * @param other TimeRange to copy
     */
    public TimeRange(TimeRange other) {
        this.fromHour = other.getFromHour();
        this.fromMinute = other.getFromMinute();
        this.untilHour = other.getUntilHour();
        this.untilMinute = other.getUntilMinute();
    }

    /**
     * Encode this TimeRange to a String
     * @return encoded TimeRange
     */
    public String encode() {
        return String.format("%02d:%02d-%02d:%02d", fromHour, fromMinute, untilHour, untilMinute);
    }

    public int getFromHour() {
        return fromHour;
    }

    public int getFromMinute() {
        return fromMinute;
    }

    public int getUntilHour() {
        return untilHour;
    }

    public int getUntilMinute() {
        return untilMinute;
    }

    /**
     * Update this TimeRange
     * @param fromHour the fromHour
     * @param fromMinute the fromMinute
     * @param untilHour the untilHour
     * @param untilMinute the untilMinute
     */
    public void update(int fromHour, int fromMinute, int untilHour, int untilMinute) {
        this.fromHour = fromHour;
        this.fromMinute = fromMinute;
        this.untilHour = untilHour;
        this.untilMinute = untilMinute;
    }

    /**
     * Decodes string to integer number, with upper and lower bound
     * @param string to decode
     * @param min lower bound; result will be minimal this value
     * @param max upper bound; result will be maximal this value
     * @return number decoded from String and bounded
     */
    private int decodeInteger(String string, int min, int max) {
        int number = Integer.parseInt(string);
        number = Math.max(number, min);
        number = Math.min(number, max);
        return number;
    }
}
