package no.java.incogito.ems.client;

import fj.F;
import fj.F2;
import static fj.Function.compose;
import static fj.Function.curry;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import static fj.data.Option.fromNull;
import no.java.ems.client.EventsClient;
import no.java.ems.client.SessionsClient;
import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
import no.java.ems.service.EmsService;
import no.java.incogito.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
public class EmsWrapper {

    private final EventsClient eventsClient;
    private final SessionsClient sessionsClient;

    @Autowired
    public EmsWrapper(EmsService emsService) {
        eventsClient = emsService.getEventsClient();
        sessionsClient = emsService.getSessionsClient();
    }

    // -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    public List<Event> listEvents() {
        return iterableList(eventsClient.listEvents());
    }

    public F<String, Option<Event>> findEventByName = new F<String, Option<Event>>() {
        public Option<Event> f(String eventName) {
            return iterableList(eventsClient.listEvents()).
                    find(compose(Functions.equals.f(eventName), EmsFunctions.eventName));
        }
    };

    // -----------------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------------

    public F<String, Session> getSessionById = new F<String, Session>() {
        public Session f(String id) {
            return fromNull(sessionsClient.getSession(id)).some();
        }
    };

    public F<String, List<String>> findSessionIdsByEventId = new F<String, List<String>>() {
        public List<String> f(String eventId) {
            return iterableList(sessionsClient.findSessionIdsByEvent(eventId));
        }
    };

    public F<String, F<String, List<String>>> findSessionIdsByEventIdAndTitle = curry(new F2<String, String, List<String>>() {
        public List<String> f(String eventId, String title) {
            return iterableList(sessionsClient.findSessionsByTitle(eventId, title));
        }
    });
}
