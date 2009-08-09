package no.java.incogito.domain;

import fj.F;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import fj.pre.Ord;
import no.java.incogito.Enums;
import no.java.incogito.domain.Session.Level;

import java.io.File;
import java.util.UUID;
import java.net.URI;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Event {
    public static final TreeMap<Level, File> emptyLevelIconMap = TreeMap.empty(Enums.<Level>ord());
    public static final TreeMap<String, File> emptyLabelIconMap = TreeMap.empty(Ord.stringOrd);

    public final EventId id;

    public final String name;

    public final Option<String> welcome;

    public final List<Room> rooms;

    public final TreeMap<Level, File> levelIconFiles;

    public final TreeMap<String, File> labelIconFiles;

    public Event(EventId id, String name, Option<String> welcome, List<Room> rooms, TreeMap<Level, File> levelIconFiles, TreeMap<String, File> labelIconFiles) {
        this.id = id;
        this.name = name;
        this.welcome = welcome;
        this.rooms = rooms;
        this.levelIconFiles = levelIconFiles;
        this.labelIconFiles = labelIconFiles;
    }

    public static class EventId extends Id {
        public static final Ord<EventId> ord = Ord.comparableOrd();
        public static final F<String, EventId> eventId = new F<String, EventId>() {
            public EventId f(String value) {
                return eventId(value);
            }
        };

        private EventId(String value) {
            super(value);
        }

        public static EventId eventId(String value) {
            return new EventId(value);
        }
    }

    public static class Id implements Comparable {

        public final UUID value;

        private Id(String value) {
            this.value = UUID.fromString(value);
        }

        public int compareTo(Object o) {
            return value.compareTo(((Id) o).value);
        }
    }
}
