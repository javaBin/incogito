package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.F3;
import static fj.Function.compose;
import static fj.Function.curry;
import fj.P1;
import fj.P2;
import fj.Unit;
import fj.control.parallel.Callables;
import fj.data.Either;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.join;
import static fj.data.Option.some;
import static fj.data.Option.somes;
import fj.data.TreeMap;
import static no.java.incogito.application.EmsFunctions.eventFromEms;
import static no.java.incogito.application.EmsFunctions.sessionFromEms;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;
import static no.java.incogito.application.IncogitoConfiguration.unconfigured;
import static no.java.incogito.application.OperationResult.notFound;
import static no.java.incogito.application.OperationResult.ok;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final File incogitoHome;
    private final UserClient userClient;
    private final EmsWrapper emsWrapper;

    /**
     * This is not ideal, but a configuration is required to load the configuration
     */
    private IncogitoConfiguration configuration = unconfigured;

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

            TreeMap<String, Label> labels = event.labels;
            logger.info(" Labels: (" + labels.size() + ")");
            for (P2<String, Label> label : labels) {
                logger.info("  " + label._2().displayName + " (" + label._2().id + "), icon: " + label._2().iconFile.getName());
            }

            TreeMap<LevelId, Level> levels = event.levels;
            logger.info(" Levels: (" + levels.size() + ")");
            for (P2<LevelId, Level> level : levels) {
                logger.info("  " + level._2().displayName + " (" + level._2().id + "), icon: " + level._2().iconFile.getName());
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
        this.configuration = new ConfigurationLoaderService(emsWrapper).loadConfiguration(incogitoHome, configuration);
    }

    public OperationResult<List<Event>> getEvents() {
        List<Option<no.java.ems.domain.Event>> emsEvents = getConfiguration().eventConfigurations.
            map(compose(emsWrapper.findEventByName, EventConfiguration.name_));

        List<Option<Event>> events = Option.<no.java.ems.domain.Event>somes(emsEvents).
            map(eventFromEms.f(configuration));

        return ok(Option.somes(events));
    }

    public OperationResult<Event> getEventByName(String eventName) {
        Option<Event> eventOption = Option.join(emsWrapper.
            findEventByName.f(eventName).
            map(eventFromEms.f(configuration)));

        return eventOption.
            map(OperationResult.<Event>ok_()).
            orSome(OperationResult.<Event>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<List<Session>> getSessions(String eventName) {

        return emsWrapper.findEventByName.f(eventName).bind(eventFromEms.f(configuration)).
            map(compose(OperationResult.<List<Session>>ok_(), getSessionsForEvent)).
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
                    bind(sessionFromEms.f(event)).
                    map(OperationResult.<Session>ok_()).
                    orSome(OperationResult.<Session>notFound("Could not find session with title '" + sessionTitle + "' not found."));
            }
        }));
    }

    public OperationResult<Session> getSession(String eventName, final SessionId sessionId) {
        Option<Event> x = emsWrapper.findEventByName.f(eventName).bind(eventFromEms.f(configuration));
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
        Option<Event> event = emsWrapper.findEventByName.f(eventName).bind(eventFromEms.f(configuration));

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
    //
    // -----------------------------------------------------------------------

    F<Event, List<Session>> getSessionsForEvent = new F<Event, List<Session>>() {
        public List<Session> f(Event event) {
            return somes(emsWrapper.findSessionsByEventId.f(event.id.toString()).
                map(sessionFromEms.f(event)));
        }
    };

    F<Event, F<List<Session>, F<Option<User>, Schedule>>> createSchedule = curry(new F3<Event, List<Session>, Option<User>, Schedule>() {
        public Schedule f(Event event, List<Session> sessions, Option<User> userOption) {
            return new Schedule(event, sessions, userOption.map(User.sessionAssociations_).orSome(User.emptySessionAssociations));
        }
    });
}
