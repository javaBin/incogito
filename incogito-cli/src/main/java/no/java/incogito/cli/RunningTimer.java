package no.java.incogito.cli;

import fj.data.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RunningTimer {
    public final long start;
    private List<Lap> laps;

    public RunningTimer(long start, List<Lap> laps) {
        this.start = start;
        this.laps = laps;
    }

    public class Lap
    {
        public final long start;
        public final long interval;

        public Lap(long now, long start) {
            this.start = start;
            this.interval = now - start;
        }
    }

    public static RunningTimer runningTimer() {
        return new RunningTimer(System.currentTimeMillis(), List.<Lap>nil());
    }

    public List<Lap> laps() {
        return laps;
    }

    public Lap lap() {
        Lap lap;
        if(laps.isEmpty()) {
            lap = new Lap(System.currentTimeMillis(), start);
        }
        else {
            lap = new Lap(System.currentTimeMillis(), laps.head().start);
        }
        laps = laps.cons(lap);
        return lap;
    }
}
