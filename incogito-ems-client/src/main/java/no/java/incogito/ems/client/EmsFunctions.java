package no.java.incogito.ems.client;

import fj.F;
import no.java.ems.external.v2.*;

/**
 * First-order functions on the EMS domain objects.
 *
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsFunctions {
    public static final F<EventV2, String> eventUuid = new F<EventV2, String>() {
        public String f(EventV2 event) {
            return event.getUuid();
        }
    };

    public static final F<EventV2, String> eventName = new F<EventV2, String>() {
        public String f(EventV2 event) {
            return event.getName();
        }
    };

    public static final F<RoomV2, String> roomName = new F<RoomV2, String>() {
        public String f(RoomV2 room) {
            return room.getName();
        }
    };

    public static final F<RoomV2, String> roomDescription = new F<RoomV2, String>() {
        public String f(RoomV2 room) {
            return room.getDescription();
        }
    };
}
