package no.java.incogito.domain;

import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import fj.pre.Ord;
import static fj.pre.Ord.stringOrd;
import fj.P2;
import no.java.incogito.Enums;
import no.java.incogito.domain.Level.LevelId;

import java.util.UUID;

import org.joda.time.LocalDate;

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

    public final Option<String> frontpageContent;

    public final List<Room> presentationRooms;

    public final List<P2<LocalDate, List<Room>>> roomsByDate;

    public final TreeMap<LevelId, Level> levels;

    public final TreeMap<String, Label> labels;

    public final TreeMap<String, Label> emsIndexedLabels;

    public Event(EventId id, String name, Option<String> blurb, Option<String> frontpageContent,
                 List<Room> presentationRooms, List<P2<LocalDate, List<Room>>> roomsByDate, TreeMap<LevelId, Level> levels,
                 TreeMap<String, Label> labels) {
        this.id = id;
        this.name = name;
        this.blurb = blurb;
        this.frontpageContent = frontpageContent;
        this.presentationRooms = presentationRooms;
        this.roomsByDate = roomsByDate;
        this.levels = levels;
        this.labels = labels;

        TreeMap<String, Label> emsIndexedLabels = TreeMap.empty(stringOrd);
        for (Label label : labels.values()) {
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
