package com.github.trainjezelf.datastore;

/**
 * Created by ronald on 23-9-14.
 */
public class TimeRange {

    private int fromHour;
    private int fromMinute;

    private int untilHour;
    private int untilMinute;

    public TimeRange(String encoded) {
        final String[] times = encoded.split("-");
        final String[] fromPieces = times[0].split(":");
        final String[] untilPieces = times[1].split(":");
        fromHour = decodeInteger(fromPieces[0], 0, 23);
        fromMinute = decodeInteger(fromPieces[1], 0, 59);
        untilHour = decodeInteger(untilPieces[0], 0, 23);
        untilMinute = decodeInteger(untilPieces[1], 0, 59);
    }

    public TimeRange(TimeRange other) {
        this.fromHour = other.getFromHour();
        this.fromMinute = other.getFromMinute();
        this.untilHour = other.getUntilHour();
        this.untilMinute = other.getUntilMinute();
    }

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

    public void update(int fromHour, int fromMinute, int untilHour, int untilMinute) {
        this.fromHour = fromHour;
        this.fromMinute = fromMinute;
        this.untilHour = untilHour;
        this.untilMinute = untilMinute;
    }

    private int decodeInteger(String string, int min, int max) {
        int hour = Integer.parseInt(string);
        hour = Math.max(hour, min);
        hour = Math.min(hour, max);
        return hour;
    }
}
