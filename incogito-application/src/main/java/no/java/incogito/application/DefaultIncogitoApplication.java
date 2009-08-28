package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.F2;
import fj.F3;
import static fj.Function.compose;
import static fj.Function.curry;
import static fj.Function.flip;
import fj.P1;
import fj.P2;
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
import static fj.data.Option.somes;
import fj.data.TreeMap;
import fj.pre.Ord;
import fj.pre.Show;
import no.java.incogito.Enums;
import no.java.incogito.Functions;
import static no.java.incogito.Functions.throwLeft;
import no.java.incogito.IO;
import static no.java.incogito.IO.Strings.streamToString;
import no.java.incogito.PropertiesF;
import static no.java.incogito.application.IncogitoConfiguration.emptyWelcomeTexts;
import static no.java.incogito.application.OperationResult.notFound;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.CssConfiguration;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Event.EventId;
import static no.java.incogito.domain.Event.emptyLabelIconMap;
import static no.java.incogito.domain.Event.emptyLevelIconMap;
import static no.java.incogito.domain.Event.EventId.eventId;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
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
    private IncogitoConfiguration configuration = IncogitoConfiguration.unconfigured;

    @Autowired
    public DefaultIncogitoApplication(@Qualifier("incogitoHome") File incogitoHome, UserClient userClient, EmsWrapper emsWrapper) {
        this.incogitoHome = incogitoHome;
        this.userClient = userClient;
        this.emsWrapper = emsWrapper;
    }

    public void afterPropertiesSet() throws Exception {
        reloadConfiguration();

        logger.info("Configuration: ");
        logger.info("Incogito Home: " + incogitoHome);
        OperationResult<List<Event>> result = getEvents();

        if (!result.isOk()) {
            logger.info("No events configured.");
            return;
        }

        List<Event> events = result.value();
        for (Event event : events) {
            logger.info("Event: " + event.name);

            TreeMap<String, Label> labels = configuration.labels.get(event.id).orSome(IncogitoConfiguration.emptyLabelMap);
            logger.info(" Labels: (" + labels.size() + ")");
            for (P2<String, Label> label : labels) {
                logger.info("  " + label._2().displayName + " (" + label._2().id + "), icon: " + label._2().iconFile.getName());
            }

            TreeMap<LevelId, Level> levels = configuration.levels.get(event.id).orSome(IncogitoConfiguration.emptyLevelMap);
            logger.info(" Levels: (" + levels.size() + ")");
            for (P2<LevelId, Level> level : levels) {
                logger.info("  " + level._2().displayName + " (" +  level._2().id + "), icon: " + level._2().iconFile.getName());
            }
        }
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
//        logger.info("Reloading configuration from: " + props);
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

        List<File> eventDirectories = Functions.File_listFiles.f(new File(etc, "events"));

        TreeMap<EventId, String> welcomeTexts = emptyWelcomeTexts;
        TreeMap<EventId, TreeMap<String, Label>> labelMapMap = IncogitoConfiguration.unconfigured.labels;
        TreeMap<EventId, TreeMap<LevelId, Level>> levelMapMap = IncogitoConfiguration.unconfigured.levels;

        // TODO: Check for icons on disk and put those in a map, including alternative texts for each image
        // TODO: Load the order of the rooms
        // TODO: Load "extra" room sessions which are special like "lunch" and "party zone"
        // TODO: Consider switching to reading a <event id>.xml file if it exist and use that as configuration
        for (final File eventDirectory : eventDirectories) {
            String eventName = eventDirectory.getName();

            final TreeMap<String, String> eventProperties = Callables.option(IO.<TreeMap<String, String>>runFileInputStream_().
                    f(PropertiesF.loadPropertiesAsMap).
                    f(new File(eventDirectory, "event.properties")))._1().orSome(TreeMap.<String, String>empty(Ord.stringOrd));

            OperationResult<Event> eventOperationResult = getEventByName(eventName);

            if (!eventOperationResult.isOk()) {
                logger.warn("Unknown event: '" + eventName + "'.");
                continue;
            }

            Event event = eventOperationResult.value();

            Option<String> welcomeText = some(new File(eventDirectory, "welcome.txt")).
                    filter(Functions.File_canRead).
                    map(IO.<String>runFileInputStream_().f(streamToString)).
                    bind(compose(P1.<Option<String>>__1(), Callables.<String>option()));

            TreeMap<String, Label> labelMap = List.list(eventProperties.get("labels").orSome("").split(",")).
                    map(Functions.trim).
                    foldLeft(new F2<TreeMap<String, Label>, String, TreeMap<String, Label>>() {
                        public TreeMap<String, Label> f(TreeMap<String, Label> labelIcons, String emsId) {
                            // If the configuration file contain an ".id" element, use that as the internal id
                            String id = eventProperties.get(emsId + ".id").orSome(emsId);

                            if(id.indexOf(' ') != -1){
                                logger.warn("Invalid id for ems label '" + emsId + "'. Override the id by adding a '" + emsId + ".id' property to the event configuration.");
                                return labelIcons;
                            }

                            File iconFile = new File(eventDirectory, "labels/" + id + ".png");

                            Option<Label> label = some(id).bind(some(emsId), eventProperties.get(emsId + ".displayName"), Option.iif(Functions.File_canRead, iconFile), Label.label_);

                            if (label.isSome()) {
                                return labelIcons.set(label.some().id, label.some());
                            }

                            logger.warn("Could not find file for label: " + id);

                            return labelIcons;
                        }
                    }, emptyLabelIconMap);

            TreeMap<LevelId, Level> levelMap = List.list(eventProperties.get("levels").orSome("").split(",")).
                    map(Functions.trim).
                    foldLeft(new F2<TreeMap<LevelId, Level>, String, TreeMap<LevelId, Level>>() {
                        public TreeMap<LevelId, Level> f(TreeMap<LevelId, Level> levelIcons, String id) {
                            File iconFile = new File(eventDirectory, "levels/" + id + ".png");

                            Option<Level> level = LevelId.valueOf_.f(id).bind(eventProperties.get(id + ".displayName"), Option.iif(Functions.File_canRead, iconFile), Level.level_);

                            if (level.isSome()) {
                                return levelIcons.set(level.some().id, level.some());
                            }
                            return levelIcons;
                        }
                    }, emptyLevelIconMap);

            if (welcomeText.isSome()) {
                welcomeTexts = welcomeTexts.set(event.id, welcomeText.some());
            }
            labelMapMap = labelMapMap.set(event.id, labelMap);
            levelMapMap = levelMapMap.set(event.id, levelMap);
        }

        this.configuration = new IncogitoConfiguration(baseurl, welcomeTexts, labelMapMap, levelMapMap, cssConfiguration);
    }

    public OperationResult<List<Event>> getEvents() {
        return OperationResult.ok(emsWrapper.listEvents._1().map(eventFromEms.f(configuration)));
    }

    public OperationResult<Event> getEventByName(String eventName) {
        Option<no.java.ems.domain.Event> eventOption = emsWrapper.findEventByName.f(eventName);

        if (eventOption.isNone()) {
            return notFound("Event with name '" + eventName + "' not found.");
        }

        return eventOption.
                map(compose(OperationResult.<Event>ok_(), eventFromEms.f(configuration))).
                orSome(OperationResult.<Event>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<List<Session>> getSessions(String eventName) {
        F<Event, OperationResult<List<Session>>> f = compose(
                OperationResult.<List<Session>>ok_(),
                getSessionsForEvent);

        return emsWrapper.findEventByName.f(eventName).map(eventFromEms.f(configuration)).
                map(f).
                orSome(OperationResult.<List<Session>>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<Session> getSessionByTitle(final String eventName, final String sessionTitle) {
        OperationResult<Event> event = getEventByName(eventName);

        if (!event.isOk()) {
            return notFound("Event '" + eventName + "' not found.");
        }

        return OperationResult.joinOk(event.ok().map(new F<Event, OperationResult<Session>>() {
            public OperationResult<Session> f(Event event) {
                return emsWrapper.
                        findSessionIdsByEventIdAndTitle.f(event.id.toString()).f(sessionTitle).toOption().
                        bind(emsWrapper.getSessionById).
                        bind(sessionFromEms.f(event)).
                        map(OperationResult.<Session>ok_()).
                        orSome(OperationResult.<Session>notFound("Could not find session with title '" + sessionTitle + "' not found."));
            }
        }));
    }

    public OperationResult<Session> getSession(String eventName, final SessionId sessionId) {
        Option<Event> x = emsWrapper.findEventByName.f(eventName).map(eventFromEms.f(configuration));
        return OperationResult.<Event>ok(x).ok().bind(new F<Event, Option<Session>>() {
            public Option<Session> f(Event event) {
                return emsWrapper.getSessionById.f(sessionId.value).
                        bind(sessionFromEms.f(event));
            }
        });
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

        if (user.isNone()) {
            return notFound("User with id '" + userId + "' does not exist.");
        }

        return getSchedule_(eventName, user);
    }

    public OperationResult<Schedule> getSchedule(String eventName, Option<String> userId) {
        return getSchedule_(eventName, userId.map(UserId.userId).bind(userClient.getUser));
    }

    private OperationResult<Schedule> getSchedule_(String eventName, Option<User> user) {
        Option<Event> event = emsWrapper.findEventByName.f(eventName).map(eventFromEms.f(configuration));

        Option<List<Session>> sessions = event.map(getSessionsForEvent);

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

    public OperationResult<byte[]> getPersonPhoto(String personId) {
        return join(emsWrapper.getPhoto.f(personId).
            map(compose(P1.<Option<byte[]>>__1(), Callables.<byte[]>option()))).
            map(OperationResult.<byte[]>ok_()).
            orSome(OperationResult.<byte[]>notFound("No such person '" + personId + "'."));
    }

    // -----------------------------------------------------------------------
    // Functions from EMS domain objects to Incogito domain objects
    // -----------------------------------------------------------------------

    private static F<no.java.ems.domain.Room, Room> roomFromEms = new F<no.java.ems.domain.Room, Room>() {
        public Room f(no.java.ems.domain.Room room) {
            return new Room(room.getName());
        }
    };

    private static F<IncogitoConfiguration, F<no.java.ems.domain.Event, Event>> eventFromEms = curry(new F2<IncogitoConfiguration, no.java.ems.domain.Event, Event>() {
        public Event f(IncogitoConfiguration configuration, no.java.ems.domain.Event event) {
            EventId eventId = eventId(event.getId());

            return new Event(eventId,
                    event.getName(),
                    configuration.welcomeTexts.get(eventId),
                    List.iterableList(event.getRooms()).map(roomFromEms),
                    configuration.levels.get(eventId).orSome(IncogitoConfiguration.emptyLevelMap),
                    configuration.labels.get(eventId).orSome(IncogitoConfiguration.emptyLabelMap));
        }
    });

    private static F<no.java.ems.domain.Speaker, Speaker> speakerFromEms = new F<no.java.ems.domain.Speaker, Speaker>() {
        public Speaker f(no.java.ems.domain.Speaker speaker) {
            return new Speaker(speaker.getName(), speaker.getPersonId(), fromString(speaker.getDescription()).map(WikiString.constructor));
        }
    };

    private static F<Event, F<no.java.ems.domain.Session, Option<Session>>> sessionFromEms = curry(new F2<Event, no.java.ems.domain.Session, Option<Session>>() {
        public Option<Session> f(Event event, no.java.ems.domain.Session session) {
            if (session.getTitle() == null) {
                return none();
            }

            // Hack for now until ';' is encoded in url properly
            if (session.getTitle().indexOf(';') > 0) {
                return none();
            }

            Option<LevelId> levelId = fromNull(session.getLevel()).
                    bind(Functions.compose(LevelId.valueOf, Enums.<no.java.ems.domain.Session.Level>name_()));

            F<LevelId, Option<Level>> getLevel = flip(Functions.<LevelId, Level>TreeMap_get()).f(event.levels);
            F<String, Option<Label>> getLabel = flip(Functions.<String, Label>TreeMap_get()).f(event.emsIndexedLabels);

            return some(new Session(new SessionId(session.getId()),
                    fromNull(session.getFormat()).bind(compose(Session.Format.valueOf_, Show.<no.java.ems.domain.Session.Format>anyShow().showS_())).orSome(Session.Format.Presentation),
                    session.getTitle(),
                    fromString(session.getLead()).map(WikiString.constructor),
                    fromString(session.getBody()).map(WikiString.constructor),
                    levelId.bind(getLevel),
                    fromNull(session.getTimeslot()),
                    fromNull(session.getRoom()).map(EmsFunctions.roomName),
                    somes(iterableList(session.getKeywords()).map(getLabel)),
                    iterableList(session.getSpeakers()).map(speakerFromEms),
                    List.<Comment>nil()));
        }
    });

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    F<Event, List<Session>> getSessionsForEvent = new F<Event, List<Session>>() {
        public List<Session> f(Event event) {
            return somes(somes(emsWrapper.findSessionIdsByEventId.f(event.id.toString()).
                    map(emsWrapper.getSessionById)).
                    map(sessionFromEms.f(event)));
        }
    };

    F<Event, F<List<Session>, F<Option<User>, Schedule>>> createSchedule = curry(new F3<Event, List<Session>, Option<User>, Schedule>() {
        public Schedule f(Event event, List<Session> sessions, Option<User> userOption) {
            return new Schedule(event, sessions, userOption.map(User.sessionAssociations_).orSome(User.emptySessionAssociations));
        }
    });
}
