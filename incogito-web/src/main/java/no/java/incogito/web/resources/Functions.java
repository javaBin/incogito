package no.java.incogito.web.resources;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.data.List;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Session;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.servlet.IncogitoFunctions;

import java.net.URI;
import java.net.URISyntaxException;

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

    public static final F<String, F<Session, URI>> sessionToURL = curry(new F2<String, Session, URI>() {
        public URI f(String baseurl, Session session) {
            try {
                return new URI(baseurl + "/" + IncogitoFunctions.urlEncode(session.getTitle()));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    });
}
