package de.uniluebeck.itm.netty.handlerstack.util;

import java.util.concurrent.TimeUnit;

public class DurationPlusUnit {

    private final long duration;
    private final TimeUnit unit;

    public DurationPlusUnit(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @return the unit
     */
    public TimeUnit getUnit() {
        return unit;
    }

    public long toDays() {
        return unit.toDays(duration);
    }

    public long toHours() {
        return unit.toHours(duration);
    }

    public long toMicros() {
        return unit.toMicros(duration);
    }

    public long toMillis() {
        return unit.toMillis(duration);
    }

    public long toMinutes() {
        return unit.toMinutes(duration);
    }

    public long toNanos() {
        return unit.toNanos(duration);
    }

    public long toSeconds() {
        return unit.toSeconds(duration);
    }

    public String toString() {
        return duration + unit.toString();
    }

}
