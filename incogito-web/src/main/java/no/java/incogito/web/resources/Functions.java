package no.java.incogito.web.resources;

import fj.F;
import fj.data.List;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Session;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.SessionXml;

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

    public static final F<Session, SessionXml> sessionToXml = new F<Session, SessionXml>() {
        public SessionXml f(Session session) {
            return new SessionXml(session.title);
        }
    };
}
