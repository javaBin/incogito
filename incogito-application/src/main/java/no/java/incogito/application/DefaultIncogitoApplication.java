package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.F2;
import fj.F3;
import fj.Function;
import static fj.Function.compose;
import static fj.Function.curry;
import static fj.Function.flip;
import fj.P1;
import fj.Unit;
import fj.control.parallel.Callables;
import fj.data.Either;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.fromString;
import static fj.data.Option.join;
import static fj.data.Option.none;
import static fj.data.Option.some;
import fj.data.TreeMap;
import fj.pre.Show;
import no.java.incogito.Enums;
import no.java.incogito.Functions;
import static no.java.incogito.Functions.throwLeft;
import no.java.incogito.IO;
import static no.java.incogito.IO.Strings.streamToString;
import no.java.incogito.PropertiesF;
import static no.java.incogito.application.IncogitoConfiguration.emptyLabelIconMaps;
import static no.java.incogito.application.IncogitoConfiguration.emptyLevelIconMaps;
import static no.java.incogito.application.IncogitoConfiguration.emptyWelcomeTexts;
import static no.java.incogito.application.OperationResult.notFound;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.CssConfiguration;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Event.EventId;
import static no.java.incogito.domain.Event.emptyLabelIconMap;
import static no.java.incogito.domain.Event.emptyLevelIconMap;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.Session.Level;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import no.java.incogito.domain.WikiString;
import no.java.incogito.ems.client.EmsFunctions;
import static no.java.incogito.ems.client.EmsFunctions.eventId;
import no.java.incogito.ems.client.EmsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(DefaultIncogitoApplication.class);
    private final File incogitoHome;
    private final UserClient userClient;
    private final EmsWrapper emsWrapper;

    /**
     * This is not ideal, but a configuration is required to load the configuration
     */
    private IncogitoConfiguration configuration = IncogitoConfiguration.unconfigured();

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

        F<String, Option<Double>> parseDouble = compose(Functions.<NumberFormatException, Double>Either_rightToOption_(), Functions.parseDouble);

        double sessionEmStart = properties.get("sessionEmStart").bind(parseDouble).orSome(CssConfiguration.defaultCssConfiguration.sessionEmStart);
        double emPerMinute = properties.get("emPerMinute").bind(parseDouble).orSome(CssConfiguration.defaultCssConfiguration.emPerMinute);
        double emPerRoom = properties.get("emPerRoom").bind(parseDouble).orSome(CssConfiguration.defaultCssConfiguration.emPerRoom);

        CssConfiguration cssConfiguration = new CssConfiguration(sessionEmStart, emPerMinute, emPerRoom);

        List<File> eventDirectories = Functions.listFiles.f(new File(etc, "events"));

        TreeMap<EventId, String> welcomeTexts = emptyWelcomeTexts;
        TreeMap<EventId, TreeMap<String, File>> labelIconMaps = emptyLabelIconMaps;
        TreeMap<EventId, TreeMap<Level, File>> levelIconMaps = emptyLevelIconMaps;

        // TODO: Check for icons on disk and put those in a map, including alternative texts for each image
        // TODO: Load the order of the rooms
        // TODO: Load "extra" room sessions which are special like "lunch" and "party zone"
        // TODO: Consider switching to reading a <event id>.xml file if it exist and use that as configuration
        for (File eventDirectory : eventDirectories) {
            String eventName = eventDirectory.getName();
            logger.debug("Loading " + eventName + "...");

            OperationResult<Event> eventOperationResult = getEventByName(eventName);

            if (!eventOperationResult.isOk()) {
                logger.warn("Unknown event: '" + eventName + "'.");
            }

            Event event = eventOperationResult.value();

            Option<String> welcomeText = some(new File(eventDirectory, "welcome.txt")).
                    filter(Functions.canRead).
                    map(IO.<String>runFileInputStream_().f(streamToString)).
                    bind(compose(P1.<Option<String>>__1(), Callables.<String>option()));

            List<File> labelFiles = Option.iif(Functions.isDirectory, new File(eventDirectory, "labels")).
                    map(Functions.listFiles).
                    orSome(List.<File>nil()).
                    filter(compose(Functions.String_endsWith.f(".png"), Functions.File_getName));

            Show.listShow(Functions.File_show).println(Functions.listFiles.f(new File(eventDirectory, "levels")));

            List<File> levelFiles = Option.iif(Functions.isDirectory, new File(eventDirectory, "levels")).
                    map(Functions.listFiles).
                    orSome(List.<File>nil()).
                    filter(compose(Functions.String_endsWith.f(".png"), Functions.File_getName));

            TreeMap<String, File> labelIcons = labelFiles.foldLeft(new F2<TreeMap<String, File>, File, TreeMap<String, File>>() {
                public TreeMap<String, File> f(TreeMap<String, File> labelIcons, File file) {
                    String name = file.getName();
                    return labelIcons.set(name.substring(0, name.length() - 4), file);
                }
            }, emptyLabelIconMap);

            TreeMap<Level, File> levelIcons = emptyLevelIconMap;

            for (File file : levelFiles) {
                String name = file.getName();
                name = name.substring(0, name.length() - 4);

                Option<Level> levelOption = Level.valueOf.f(name);

                if (levelOption.isNone()) {
                    continue;
                }

                levelIcons = levelIcons.set(levelOption.some(), file);
            }

            if (welcomeText.isSome()) {
                welcomeTexts = welcomeTexts.set(event.id, welcomeText.some());
            }
            labelIconMaps = labelIconMaps.set(event.id, labelIcons);
            levelIconMaps = levelIconMaps.set(event.id, levelIcons);
        }

        this.configuration = new IncogitoConfiguration(baseurl, welcomeTexts, levelIconMaps, labelIconMaps, cssConfiguration);
    }

    public OperationResult<List<Event>> getEvents() {
        return OperationResult.ok(emsWrapper.listEvents().map(eventFromEms.f(configuration)));
    }

    public OperationResult<Event> getEventByName(String eventName) {
        Option<no.java.ems.domain.Event> eventOption = emsWrapper.findEventByName.f(eventName);

        if(eventOption.isNone())  {
            return notFound("Event with name '" + eventName + "' not found.");
        }

        return eventOption.
                map(compose(OperationResult.<Event>ok_(), eventFromEms.f(configuration))).
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
            return notFound("User with id '" + userId + "' not found.");
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
            return notFound("User with id '" + userId + "' does not exist.");
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

        Option<Event> event = emsEvent.map(eventFromEms.f(configuration));
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

    private static  F<no.java.ems.domain.Room, Room> roomFromEms = new F<no.java.ems.domain.Room, Room>() {
        public Room f(no.java.ems.domain.Room room) {
            return new Room(room.getName());
        }
    };

    private static F<IncogitoConfiguration, F<no.java.ems.domain.Event, Event>> eventFromEms = curry(new F2<IncogitoConfiguration, no.java.ems.domain.Event, Event>() {
        public Event f(IncogitoConfiguration configuration, no.java.ems.domain.Event event) {
            EventId eventId = EventId.eventId(event.getId());

            return new Event(eventId,
                    event.getName(),
                    configuration.welcomeTexts.get(eventId),
                    List.iterableList(event.getRooms()).map(roomFromEms),
                    configuration.getLevelIcons(eventId),
                    configuration.getLabelIcons(eventId));
        }
    });

    private static F<no.java.ems.domain.Speaker, Speaker> speakerFromEms = new F<no.java.ems.domain.Speaker, Speaker>() {
        public Speaker f(no.java.ems.domain.Speaker speaker) {
            return new Speaker(speaker.getName(), fromString(speaker.getDescription()).map(WikiString.constructor));
        }
    };

    private static F<no.java.ems.domain.Session, Option<Session>> sessionFromEms = new F<no.java.ems.domain.Session, Option<Session>>() {
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
                    fromNull(session.getLevel()).bind(Functions.compose(Level.valueOf, Enums.<no.java.ems.domain.Session.Level>name_())),
                    fromNull(session.getTimeslot()),
                    fromNull(session.getRoom()).map(EmsFunctions.roomName),
                    iterableList(session.getKeywords()),
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
