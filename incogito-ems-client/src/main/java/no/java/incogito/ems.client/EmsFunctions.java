package no.java.incogito.ems.client;

import no.java.ems.domain.Event;
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
}
