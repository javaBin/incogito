package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.F2;
import fj.F3;
import fj.Function;
import static fj.Function.compose;
import static fj.Function.curry;
import static fj.Function.flip;
import fj.Unit;
import fj.data.Either;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.fromString;
import static fj.data.Option.none;
import static fj.data.Option.some;
import static fj.data.Option.join;
import fj.data.TreeMap;
import fj.function.Booleans;
import fj.function.Strings;
import no.java.incogito.Functions;
import static no.java.incogito.Functions.throwLeft;
import no.java.incogito.IO;
import static no.java.incogito.IO.Strings.streamToString;
import no.java.incogito.PropertiesF;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import no.java.incogito.domain.WikiString;
import no.java.incogito.ems.client.EmsFunctions;
import static no.java.incogito.ems.client.EmsFunctions.eventId;
import no.java.incogito.ems.client.EmsWrapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component("incogitoApplication")
public class DefaultIncogitoApplication implements IncogitoApplication, InitializingBean {
    private final Logger logger = Logger.getLogger(DefaultIncogitoApplication.class);
    private final File incogitoHome;
    private final UserClient userClient;
    private final EmsWrapper emsWrapper;

    private IncogitoConfiguration configuration;

    @Autowired
    public DefaultIncogitoApplication(@Qualifier("incogitoHome") File incogitoHome, UserClient userClient, EmsWrapper emsWrapper) {
        this.incogitoHome = incogitoHome;
        this.userClient = userClient;
        this.emsWrapper = emsWrapper;
    }

    public void afterPropertiesSet() throws Exception {
        reloadConfiguration();
    }

    // -----------------------------------------------------------------------
    // IncogitoApplication Implementation
    // -----------------------------------------------------------------------

    public IncogitoConfiguration getConfiguration() {
        return configuration;
    }

    /*
     * TODO: Consider improving the logging here.
     * Set the initial logger to log to debug() and log only any changes to info(). That way there will be no repeated
     * logging output, but it will be possible to turn it on to check that the application is working.
     */
    public void reloadConfiguration() throws Exception {
        File props = new File(incogitoHome, "etc/incogito.properties").getAbsoluteFile();
        logger.info("Reloading configuration from: " + props);
        File etc = props.getParentFile();

        TreeMap<String, String> properties = IO.<TreeMap<String, String>>runFileInputStream_().
                f(PropertiesF.loadPropertiesAsMap).
                f(props).call();

        @SuppressWarnings({"ThrowableInstanceNeverThrown"})
        String baseurl = throwLeft(properties.get("baseurl").toEither(new Exception("Missing required property: baseurl")));

        List<EventId> events = properties.get("events").
                bind(Option.fromString()).
                map(Functions.split.f(",")).orSome(List.<String>nil()).
                map(Functions.trim).
                filter(compose(Booleans.not, Strings.isEmpty)).
                map(EventId.eventId);

        TreeMap<EventId, String> welcomeTexts = TreeMap.empty(EventId.ord);

        // TODO: Load the order of the rooms
        // TODO: Load "extra" room sessions which are special like "lunch" and "party zone"
        // TODO: Consider switching to reading a <event id>.xml file if it exist and use that as configuration
        for (EventId event : events) {
            logger.debug("Loading " + event.value.toString() + "...");

            Option<File> fileOption = properties.get("event." + event.value.toString() + ".welcome").
                    map(Functions.newFile.f(etc)).
                    filter(Functions.canRead);

            if (fileOption.isNone()) {
                continue;
            }

            logger.debug("Welcome file: " + fileOption.some());

            String welcomeHtml = IO.<String>runFileInputStream_().f(streamToString).f(fileOption.some()).call();

            welcomeTexts = welcomeTexts.set(event, welcomeHtml);
        }

        this.configuration = new IncogitoConfiguration(baseurl, welcomeTexts);
    }

    public OperationResult<List<Event>> getEvents() {
        F<EventId, Option<String>> getter = flip(Functions.<EventId,String>TreeMap_get()).f(configuration.welcomeTexts);
        return OperationResult.ok(emsWrapper.listEvents().map(eventFromEms.f(getter)));
    }

    public OperationResult<Event> getEventByName(String eventName) {
        F<EventId, Option<String>> getter = flip(Functions.<EventId,String>TreeMap_get()).f(configuration.welcomeTexts);
        return emsWrapper.findEventByName.f(eventName).
                map(compose(OperationResult.<Event>ok_(), eventFromEms.f(getter))).
                orSome(OperationResult.<Event>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<List<Session>> getSessions(String eventName) {
        F<no.java.ems.domain.Event, OperationResult<List<Session>>> f = compose(
                OperationResult.<List<Session>>ok_(),
                getSessionsForEvent());

        return emsWrapper.findEventByName.f(eventName).
                map(f).
                orSome(OperationResult.<List<Session>>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<Session> getSessionByTitle(String eventName, String sessionTitle) {
        return findSession().f(eventName).f(sessionTitle).
                map(OperationResult.<Session>ok_()).
                orSome(OperationResult.<Session>notFound("Could not find session with title '" + sessionTitle + "' not found."));
    }

    public OperationResult<Session> getSession(String eventName, SessionId sessionId) {
        return emsWrapper.getSessionById.f(sessionId.value).bind(sessionFromEms).
                map(OperationResult.<Session>ok_()).
                orSome(OperationResult.<Session>notFound("Could not find session: '" + sessionId + "'."));
    }

    public OperationResult<User> createUser(User user) {
        Either<User, User> either = userClient.getUser(user.id).toEither(user);

        either.left().foreach(new Effect<User>() {
            public void e(User user) {
                userClient.setUser(user);
            }
        });

        return either.either(OperationResult.<User>ok_(),
                OperationResult.<User>conflict_("User with id '" + user.id + "' already exist."));
    }

    public OperationResult<Unit> removeUser(UserId userId) {
        if (userClient.removeUser(userId)) {
            return OperationResult.emptyOk();
        } else {
            return OperationResult.notFound("User with id '" + userId + "' not found.");
        }
    }

    public OperationResult<User> getUser(UserId userId) {
        return userClient.getUser(userId).
                map(OperationResult.<User>ok_()).
                orSome(OperationResult.<User>notFound("User with id '" + userId.value + "' does not exist."));
    }

    public OperationResult<Schedule> getSchedule(String eventName, String userId) {
        Option<User> user = userClient.getUser(new UserId(userId));

        if(user.isNone()) {
            return OperationResult.notFound("User with id '" + userId + "' does not exist.");
        }

        return getSchedule_(eventName, user);
    }

    public OperationResult<Schedule> getSchedule(String eventName, Option<String> userId) {
        return getSchedule_(eventName, userId.map(UserId.userId).bind(userClient.getUser));
    }

    private OperationResult<Schedule> getSchedule_(String eventName, Option<User> user) {
        Option<no.java.ems.domain.Event> emsEvent = emsWrapper.findEventByName.f(eventName);

        F<no.java.ems.domain.Event, List<Session>> f = Functions.compose(
                DefaultIncogitoApplication.<Session>filterAndRemove(),
                List.<no.java.ems.domain.Session, Option<Session>>map_().f(sessionFromEms),
                DefaultIncogitoApplication.<no.java.ems.domain.Session>filterAndRemove(),
                List.<String, Option<no.java.ems.domain.Session>>map_().f(emsWrapper.getSessionById),
                emsWrapper.findSessionIdsByEventId,
                eventId);

        F<EventId, Option<String>> getter = flip(Functions.<EventId,String>TreeMap_get()).f(configuration.welcomeTexts);

        Option<Event> event = emsEvent.map(eventFromEms.f(getter));
        Option<List<Session>> sessions = emsEvent.map(f);

        return event.bind(sessions, some(user), createSchedule).
                map(OperationResult.<Schedule>ok_()).
                orSome(OperationResult.<Schedule>$notFound("Event '" + eventName + "' not found."));
    }

    public OperationResult<User> setInterestLevel(String userName, String eventName, SessionId sessionId, InterestLevel interestLevel) {

        // TODO: This is the way it should be
//        OperationResult<User> result = OperationResult.ok(userClient.getUser(userId)).
//                ok().map(updateInterestLevelOnUser.f(userSessionAssociation));
//
//        result.foreach(userClient.setUser);
//
//        return result.map(OperationResult.<User>ok_()).
//                orSome(OperationResult.<User>$notFound("User '" + userId.value + "' not found."));

        Option<User> user = userClient.getUser(new UserId(userName)).
                map(User.setInterestLevel.f(sessionId).f(interestLevel));

        // TODO: Only save the user if needed
        user.foreach(userClient.setUser);

        return user.map(OperationResult.<User>ok_()).
                orSome(OperationResult.<User>$notFound("User '" + userName + "' not found."));
    }

    // -----------------------------------------------------------------------
    // Functions from EMS domain objects to Incogito domain objects
    // -----------------------------------------------------------------------

    public F<no.java.ems.domain.Room, Room> roomFromEms = new F<no.java.ems.domain.Room, Room>() {
        public Room f(no.java.ems.domain.Room room) {
            return new Room(room.getName());
        }
    };

    F<F<EventId, Option<String>>, F<no.java.ems.domain.Event, Event>> eventFromEms = curry(new F2<F<EventId, Option<String>>, no.java.ems.domain.Event, Event>() {
        public Event f(F<EventId, Option<String>> welcomeTextGetter, no.java.ems.domain.Event event) {
            EventId id = EventId.eventId(event.getId());
            return new Event(id, event.getName(), welcomeTextGetter.f(id), List.iterableList(event.getRooms()).map(roomFromEms));
        }
    });

    F<no.java.ems.domain.Speaker, Speaker> speakerFromEms = new F<no.java.ems.domain.Speaker, Speaker>() {
        public Speaker f(no.java.ems.domain.Speaker speaker) {
            return new Speaker(speaker.getName(), fromString(speaker.getDescription()).map(WikiString.constructor));
        }
    };

    F<no.java.ems.domain.Session, Option<Session>> sessionFromEms = new F<no.java.ems.domain.Session, Option<Session>>() {
        public Option<Session> f(no.java.ems.domain.Session session) {
            if(session.getTitle() == null) {
                return none();
            }

            // Hack for now until ';' is encoded in url properly
            if(session.getTitle().indexOf(';') > 0) {
                return none();
            }

            return some(new Session(new SessionId(session.getId()),
                    session.getTitle(),
                    fromString(session.getLead()).map(WikiString.constructor),
                    fromString(session.getBody()).map(WikiString.constructor),
                    fromNull(session.getTimeslot()),
                    fromNull(session.getRoom()).map(EmsFunctions.roomName),
                    iterableList(session.getTags()),
                    iterableList(session.getSpeakers()).map(speakerFromEms),
                    List.<Comment>nil()));
        }
    };

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    F<no.java.ems.domain.Event, List<Session>> getSessionsForEvent() {
        return Functions.compose(
                DefaultIncogitoApplication.<Session>filterAndRemove(),
                List.<no.java.ems.domain.Session, Option<Session>>map_().f(sessionFromEms),
                DefaultIncogitoApplication.<no.java.ems.domain.Session>filterAndRemove(),
                List.<String, Option<no.java.ems.domain.Session>>map_().f(emsWrapper.getSessionById),
                emsWrapper.findSessionIdsByEventId,
                eventId);
    }

    private F<String, F<String, Option<Session>>> findSession() {
        return curry(new F2<String, String, Option<Session>>() {
            public Option<Session> f(final String eventName, final String sessionTitle) {
                F<no.java.ems.domain.Event, Option<Session>> f = Functions.compose(
                        Functions.<Session>Option_join_(),
                        Functions.<no.java.ems.domain.Session, Option<Session>>Option_map(sessionFromEms),
                        Functions.<no.java.ems.domain.Session>Option_join_(),
                        Functions.<String, Option<no.java.ems.domain.Session>>Option_map(emsWrapper.getSessionById),
                        Functions.<String>List_toOption_(),
                        flip(emsWrapper.findSessionIdsByEventIdAndTitle).f(sessionTitle),
                        eventId);
                return join(emsWrapper.findEventByName.f(eventName).map(f));
            }
        });
    }

    F<Event, F<List<Session>, F<Option<User>, Schedule>>> createSchedule = curry( new F3<Event, List<Session>, Option<User>, Schedule>() {
        public Schedule f(Event event, List<Session> sessions, Option<User> userOption) {
            return new Schedule(event, sessions, userOption.map(User.sessionAssociations_).orSome(User.emptySessionAssociations));
        }
    });

    static <A> F<List<Option<A>>, List<A>> filterAndRemove() {
        return Function.compose(
            List.<Option<A>, A>map_().f(Functions.<A>Option_somes()),
            Functions.<Option<A>>List_filter().f(Option.<A>isSome_()));
    }
}
