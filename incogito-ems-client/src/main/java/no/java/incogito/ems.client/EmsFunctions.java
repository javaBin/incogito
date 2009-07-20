package no.java.incogito.ems.client;

import no.java.ems.domain.Event;
import no.java.ems.domain.Room;
import fj.F;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsFunctions {
    public static final F<Event, String> eventId = new F<Event, String>() {
        public String f(Event event) {
            return event.getId();
        }
    };

    public static final F<Event, String> eventName = new F<Event, String>() {
        public String f(Event event) {
            return event.getName();
        }
    };

    public static final F<Room, String> roomName = new F<Room, String>() {
        public String f(Room room) {
            return room.getName();
        }
    };

    public static final F<Room, String> roomDescription = new F<Room, String>() {
        public String f(Room room) {
            return room.getDescription();
        }
    };
}
