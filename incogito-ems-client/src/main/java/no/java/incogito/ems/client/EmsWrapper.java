package no.java.incogito.ems.client;

import fj.*;
import static fj.Function.*;
import static fj.control.parallel.Callables.bind;
import fj.data.*;
import static fj.data.Either.*;
import static fj.data.List.*;
import static fj.data.Option.*;
import fj.pre.*;
import no.java.ems.client.*;
import no.java.ems.external.v2.EmsV2F.*;
import no.java.ems.external.v2.*;
import no.java.incogito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
public class EmsWrapper {

    private final RESTfulEmsV2Client emsClient;
    private final TreeMap<String, ResourceHandle> eventUrlMap = TreeMap.empty(Ord.stringOrd);
    private final TreeMap<String, ResourceHandle> sessionUrlMap = TreeMap.empty(Ord.stringOrd);

    @Autowired
    public EmsWrapper(RESTfulEmsV2Client emsClient) {
        this.emsClient = emsClient;
    }

    // -----------------------------------------------------------------------
    // Event
    // -----------------------------------------------------------------------

    public P1<List<EventV2>> getEvents = new P1<List<EventV2>>() {
        public List<EventV2> _1() {
            try {
                return iterableList(emsClient.getEvents().getEvent());
            } catch (Exception e) {
                return nil();
            }
        }
    };

    public F<String, Either<String, EventV2>> findEventByName = new F<String, Either<String, EventV2>>() {
        public Either<String, EventV2> f(String eventName) {
            try {
                return iterableList(emsClient.getEvents().getEvent()).
                    find(compose(Functions.equals.f(eventName), EmsFunctions.eventName)).toEither("Could not find an event with the name '" + eventName + "'.");
            } catch (Exception e) {
                return left("Error while fetching events: " + e.getMessage());
            }
        }
    };

    // -----------------------------------------------------------------------
    // Session
    // -----------------------------------------------------------------------

    public F<String, Either<String, SessionV2>> getSessionById = new F<String, Either<String, SessionV2>>() {
        public Either<String, SessionV2> f(String id) {
            try {
                return sessionUrlMap.get(id).orElse(findSessionById(id)).
                    bind(emsClient.getSession_).
                    toEither("No such session: '" + id + "'.");
            } catch (Exception e) {
                return left("No such session: '" + id + "'.");
            }
        }
    };

    private P1<Option<ResourceHandle>> findSessionById(String id) {
        return new P1<Option<ResourceHandle>>() {
            @Override
            public Option<ResourceHandle> _1() {
                return none();
            }
        };
    }

    public F<String, List<SessionV2>> findSessionsByEventId = new F<String, List<SessionV2>>() {
        List<SessionV2> emptyList = nil();

        public List<SessionV2> f(String id) {
            try {
                return eventUrlMap.get(id).orElse(findEventById(id)).
                    map(emsClient.getSessions_).
                    map(SessionListV2F.getSession).
                    orSome(emptyList);
            } catch (Exception e) {
                return nil();
            }
        }
    };

    private P1<Option<ResourceHandle>> findEventById(String id) {
        return new P1<Option<ResourceHandle>>() {
            @Override
            public Option<ResourceHandle> _1() {
                return none();
            }
        };
    }

    public F<String, F<String, List<SessionV2>>> findSessionIdsByEventIdAndTitle = curry(new F2<String, String, List<SessionV2>>() {
        public List<SessionV2> f(String eventId, String title) {
            throw new RuntimeException("Not implemented");
//            try {
//                // TODO: Use search
//                return iterableList(emsClient.findSessionsByTitle(eventId, title));
//            } catch (Exception e) {
//                return nil();
//            }
        }
    });

    public F<String, Either<String, URIBinaryV2>> getPersonPhoto = new F<String, Either<String, URIBinaryV2>>() {
        public Either<String, URIBinaryV2> f(String id) {
            return left("not implemented");
//            return joinRight(fromNull(emsClient.getPerson().get(id)).
//                    toEither("No such person '" + id + "'.").
//                    right().map(getPhotoFromPerson));
        }
    };

    public static F<PersonV2, Either<String, URIBinaryV2>> getPhotoFromPerson = new F<PersonV2, Either<String, URIBinaryV2>>() {
        public Either<String, URIBinaryV2> f(PersonV2 person) {
            return fromNull(person.getPhoto()).toEither("Person does not have a photo '" + person.getUuid() + "'.");
        }
    };

    public static F<SpeakerV2, Either<String, URIBinaryV2>> getPhotoFromSpeaker = new F<SpeakerV2, Either<String, URIBinaryV2>>() {
        public Either<String, URIBinaryV2> f(SpeakerV2 speaker) {
            return fromNull(speaker.getPhoto()).toEither("Speaker does not have a photo '" + speaker.getPersonUuid() + "'.");
        }
    };

    public static F<Integer, F<SessionV2, Either<String, SpeakerV2>>> getSpeakerFromSession = curry(new F2<Integer, SessionV2, Either<String, SpeakerV2>>() {
        public Either<String, SpeakerV2> f(Integer index, SessionV2 session) {
            if (index >= session.getSpeakers().getSpeaker().size()) {
                return left("Session does not have that many speakers: " + index + "/" + session.getSpeakers().getSpeaker().size() + ".");
            }

            return fromNull(session.getSpeakers().getSpeaker().get(index)).
                    toEither("Speaker #" + index + " does not have a photo.");
        }
    });

    public static F<URIBinaryV2, Callable<byte[]>> fetchBinary = new F<URIBinaryV2, Callable<byte[]>>() {
        public Callable<byte[]> f(URIBinaryV2 binary) {
            return bind(bind(IO.Urls.fromString.f(binary.getUri()), IO.Urls.openStream), IO.ByteArrays.streamToByteArray);
        }
    };
}
