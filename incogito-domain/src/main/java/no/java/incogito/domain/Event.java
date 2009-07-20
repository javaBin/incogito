package no.java.incogito.domain;

import fj.pre.Ord;
import fj.F;

import java.util.UUID;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Event {
    public final EventId id;

    public final String name;

    public static final F<Event, String> getName = new F<Event, String>() {
        public String f(Event event) {
            return event.name;
        }
    };

    public static final F<Event, EventId> getId = new F<Event, EventId>() {
        public EventId f(Event event) {
            return event.id;
        }
    };

    public Event(EventId id, String name) {
        this.id = id;
        this.name = name;
    }

    public static EventId id(String value) {
        return new EventId(value);
    }

    public static class EventId extends Id {
        public static Ord<EventId> ord = Ord.comparableOrd();

        private EventId(String value) {
            super(value);
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
