package no.java.incogito.ems.client;

import fj.F;
import fj.F2;
import static fj.Function.compose;
import static fj.Function.curry;
import fj.P1;
import fj.data.List;
import static fj.data.List.iterableList;
import static fj.data.List.nil;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.none;
import static fj.data.Option.some;
import no.java.ems.client.PeopleClient;
import no.java.ems.domain.Binary;
import no.java.ems.domain.Event;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.service.EmsService;
import no.java.incogito.Functions;
import no.java.incogito.IO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
public class EmsWrapper {

    private final EmsService emsService;
    private final PeopleClient peopleClient;

    @Autowired
    public EmsWrapper(EmsService emsService) {
        this.emsService = emsService;
        this.peopleClient = emsService.getPeopleClient();
    }

// -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    public P1<List<Event>> getEvents = new P1<List<Event>>() {
        public List<Event> _1() {
            try {
                return iterableList(emsService.getEvents());
            } catch (Exception e) {
                return nil();
            }
        }
    };

    public F<String, Option<Event>> findEventByName = new F<String, Option<Event>>() {
        public Option<Event> f(String eventName) {
            try {
                return iterableList(emsService.getEvents()).
                    find(compose(Functions.equals.f(eventName), EmsFunctions.eventName));
            } catch (Exception e) {
                return none();
            }
        }
    };

    // -----------------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------------

    public F<String, Option<Session>> getSessionById = new F<String, Option<Session>>() {
        public Option<Session> f(String id) {
            try {
                return fromNull(emsService.getSession(id));
            } catch (Exception e) {
                return none();
            }
        }
    };

    public F<String, List<Session>> findSessionsByEventId = new F<String, List<Session>>() {
        public List<Session> f(String eventId) {
            try {
                return iterableList(emsService.getSessions(eventId));
            } catch (Exception e) {
                return nil();
            }
        }
    };

    public F<String, F<String, List<Session>>> findSessionIdsByEventIdAndTitle = curry(new F2<String, String, List<Session>>() {
        public List<Session> f(String eventId, String title) {
            try {
                return iterableList(emsService.findSessionsByTitle(eventId, title));
            } catch (Exception e) {
                return nil();
            }
        }
    });

    public F<String, Option<Callable<byte[]>>> getPhoto = new F<String, Option<Callable<byte[]>>>() {
        public Option<Callable<byte[]>> f(String id) {
            Person person = peopleClient.get(id);

            if (person == null) {
                return none();
            }

            Binary binary = person.getPhoto();

            if (binary == null) {
                return none();
            }

            InputStream inputStream = binary.getDataStream();

            return some(IO.ByteArrays.streamToByteArray.f(inputStream));
        }
    };
}
