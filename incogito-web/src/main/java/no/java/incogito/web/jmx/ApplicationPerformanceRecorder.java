package no.java.incogito.web.jmx;

import fj.P;
import fj.P2;

/**
* @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
* @version $Id$
*/
public class ApplicationPerformanceRecorder {
    private final String[] names;
    private final MutableReading[] readings;

    public ApplicationPerformanceRecorder(String[] names) {
        this.names = names;
        this.readings = new MutableReading[names.length];
        for (int i = 0, readingsLength = readings.length; i < readingsLength; i++) {
            readings[i] = new MutableReading();
        }
    }

    public static class MutableReading {
        private long invocationCount = -1;
        private long exceptionCount = -1;
        private long lastElapsedTime = -1;

        public Reading createReading() {
            return new Reading(invocationCount, exceptionCount, lastElapsedTime);
        }
    }

    public static class Reading {
        public final long invocationCount;
        public final long exceptionCount;
        public final long lastElapsedTime;

        public Reading(long invocationCount, long exceptionCount, long lastElapsedTime) {
            this.invocationCount = invocationCount;
            this.exceptionCount = exceptionCount;
            this.lastElapsedTime = lastElapsedTime;
        }
    }

    public synchronized void addOkMeasurement(String method, long elapsedTime) {
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            String name = names[i];

            if(name.equals(method)) {
                MutableReading reading = readings[i];
                reading.invocationCount++;
                reading.lastElapsedTime = elapsedTime;
            }
        }
    }

    public synchronized void addExceptionMeasurement(String method, long elapsedTime) {
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            String name = names[i];

            if(name.equals(method)) {
                MutableReading reading = readings[i];
                reading.invocationCount++;
                reading.exceptionCount++;
                reading.lastElapsedTime = elapsedTime;
            }
        }
    }

    public synchronized Reading getReading(String name) {
        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            if(names[i].equals(name)) {
                return this.readings[i].createReading();
            }
        }
        
        return null;
    }

    public synchronized P2<String, Reading>[] getReadings() {
        @SuppressWarnings({"unchecked"}) P2<String, Reading>[] readings = new P2[names.length];

        for (int i = 0, namesLength = names.length; i < namesLength; i++) {
            readings[i] = P.p(names[i], this.readings[i].createReading());
        }

        return readings;
    }
}
