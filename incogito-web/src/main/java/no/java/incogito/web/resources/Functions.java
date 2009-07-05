package no.java.incogito.web.resources;

import no.java.incogito.domain.Event;
import no.java.incogito.dto.EventXml;
import fj.F;
import fj.data.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Functions {
    public static final F<Event, EventXml> eventToXml = new F<Event, EventXml>() {
        public EventXml f(Event event) {
            return new EventXml(event.name);
        }
    };

    public static final F<List<Event>, List<EventXml>> eventListToXml = List.<Event, EventXml>map_().f(Functions.eventToXml);
}
