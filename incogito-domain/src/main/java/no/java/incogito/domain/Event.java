package no.java.incogito.domain;

import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import fj.pre.Ord;
import static fj.pre.Ord.stringOrd;
import no.java.incogito.Enums;
import no.java.incogito.domain.Level.LevelId;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.UUID;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Event {
    public static final TreeMap<LevelId, Level> emptyLevelIconMap = TreeMap.empty(Enums.<LevelId>ord());
    public static final TreeMap<String, Label> emptyLabelIconMap = TreeMap.empty(stringOrd);

    public final EventId id;
    public final String name;
    public final Option<String> blurb;
    public final List<Room> presentationRooms;
    public final List<LocalDate> dates;
    public final List<List<Room>> roomsByDate;
    public final List<List<Interval>> timeslotsByDate;
    public final TreeMap<LevelId, Level> levels;
    public final List<Label> labels;
    public final TreeMap<String, Label> labelMap;
    public final TreeMap<String, Label> emsIndexedLabels;

    public Event(EventId id, String name, Option<String> blurb,
                 List<Room> presentationRooms, List<LocalDate> dates, List<List<Room>> roomsByDate,
                 List<List<Interval>> timeslotsByDate, TreeMap<LevelId, Level> levels,
                 List<Label> labels, TreeMap<String, Label> labelMap) {
        this.id = id;
        this.name = name;
        this.blurb = blurb;
        this.presentationRooms = presentationRooms;
        this.dates = dates;
        this.roomsByDate = roomsByDate;
        this.timeslotsByDate = timeslotsByDate;
        this.levels = levels;
        this.labels = labels;
        this.labelMap = labelMap;

        TreeMap<String, Label> emsIndexedLabels = TreeMap.empty(stringOrd);
        for (Label label : labels) {
            emsIndexedLabels = emsIndexedLabels.set(label.emsId, label);
        }

        this.emsIndexedLabels = emsIndexedLabels;
    }

    public static class EventId extends Id {
        public static final Ord<EventId> ord = Ord.comparableOrd();

        private EventId(String value) {
            super(value);
        }

        public static EventId eventId(String value) {
            return new EventId(value);
        }
    }

    public static class Id implements Comparable {

        private final UUID value;

        private Id(String value) {
            this.value = UUID.fromString(value);
        }

        public int compareTo(Object o) {
            return value.compareTo(((Id) o).value);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
