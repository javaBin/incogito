package no.java.incogito.web.resources;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P1;
import fj.data.List;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Session;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.SessionXml;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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

    public static final F<URI, F<Session, SessionXml>> sessionToXml = curry(new F2<URI, Session, SessionXml>() {
        public SessionXml f(URI uri, Session session) {
            return new SessionXml(uri, session.title);
        }
    });

    public static final F<P1<UriBuilder>, F<Session, URI>> sessionToURL = curry(new F2<P1<UriBuilder>, Session, URI>() {
        public URI f(P1<UriBuilder> baseurl, Session session) {
            return baseurl._1().segment(session.getTitle()).build();
        }
    });
}
