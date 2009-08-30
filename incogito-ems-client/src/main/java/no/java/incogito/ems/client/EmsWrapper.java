package no.java.incogito.ems.client;

import fj.F;
import fj.F2;
import static fj.Function.compose;
import static fj.Function.curry;
import fj.P1;
import fj.data.Either;
import static fj.data.Either.joinRight;
import static fj.data.Either.left;
import fj.data.List;
import static fj.data.List.iterableList;
import static fj.data.List.nil;
import static fj.data.Option.fromNull;
import no.java.ems.client.PeopleClient;
import no.java.ems.client.SessionsClient;
import no.java.ems.domain.Binary;
import no.java.ems.domain.Event;
import no.java.ems.domain.Person;
import no.java.ems.domain.Session;
import no.java.ems.domain.Speaker;
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
    private final SessionsClient sessionsClient;
    private final PeopleClient peopleClient;

    @Autowired
    public EmsWrapper(EmsService emsService) {
        this.emsService = emsService;
        this.sessionsClient = emsService.getSessionsClient();
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

    public F<String, Either<String, Event>> findEventByName = new F<String, Either<String, Event>>() {
        public Either<String, Event> f(String eventName) {
            try {
                return iterableList(emsService.getEvents()).
                    find(compose(Functions.equals.f(eventName), EmsFunctions.eventName)).toEither("Could not find an event with the name '" + eventName + "'.");
            } catch (Exception e) {
                return left("Error while fetching events: " + e.getMessage());
            }
        }
    };

    // -----------------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------------

    public F<String, Either<String, Session>> getSessionById = new F<String, Either<String, Session>>() {
        public Either<String, Session> f(String id) {
            try {
                return fromNull(emsService.getSession(id)).toEither("No such session: '" + id + "'.");
            } catch (Exception e) {
                return left("No such session: '" + id + "'.");
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

    public F<String, Either<String, Binary>> getPersonPhoto = new F<String, Either<String, Binary>>() {
        public Either<String, Binary> f(String id) {
            return joinRight(fromNull(peopleClient.get(id)).
                    toEither("No such person '" + id + "'.").
                    right().map(getPhotoFromPerson));
        }
    };

    public static F<Person, Either<String, Binary>> getPhotoFromPerson = new F<Person, Either<String, Binary>>() {
        public Either<String, Binary> f(Person person) {
            return fromNull(person.getPhoto()).toEither("Person does not have a photo '" + person.getId() + "'.");
        }
    };

    public static F<Speaker, Either<String, Binary>> getPhotoFromSpeaker = new F<Speaker, Either<String, Binary>>() {
        public Either<String, Binary> f(Speaker speaker) {
            return fromNull(speaker.getPhoto()).toEither("Speaker does not have a photo '" + speaker.getId() + "'.");
        }
    };

    public static F<Integer, F<Session, Either<String, Speaker>>> getSpeakerFromSession = curry(new F2<Integer, Session, Either<String, Speaker>>() {
        public Either<String, Speaker> f(Integer index, Session session) {
            if (index >= session.getSpeakers().size()) {
                return left("Session does not have that many speakers: " + index + "/" + session.getSpeakers().size() + ".");
            }

            return fromNull(session.getSpeakers().get(index)).
                    toEither("Speaker #" + index + " does not have a photo.");
        }
    });

    public static F<Binary, Callable<byte[]>> fetchBinary = new F<Binary, Callable<byte[]>>() {
        public Callable<byte[]> f(Binary binary) {
            InputStream inputStream = binary.getDataStream();

            return IO.ByteArrays.streamToByteArray.f(inputStream);
        }
    };
}
