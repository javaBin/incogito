package no.java.incogito.application;

import fj.Bottom;
import fj.Effect;
import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P;
import fj.P1;
import fj.P2;
import fj.Unit;
import fj.control.parallel.Callables;
import fj.data.Either;
import static fj.data.Either.joinRight;
import static fj.data.Either.rights;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;
import no.java.ems.domain.Binary;
import no.java.ems.domain.Speaker;
import static no.java.incogito.Functions.compose;
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

            logger.info(" Labels: (" + event.labels.length() + ")");
            for (Label label : event.labels) {
                logger.info("  " + label.displayName + " (" + label.id + "=>" + label.emsId + "), icon: " + label.iconFile.getName());
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
        List<Either<String, no.java.ems.domain.Event>> emsEvents = getConfiguration().eventConfigurations.
            map(compose(emsWrapper.findEventByName, EventConfiguration.name_));

        List<Either<String, Event>> events = Either.<String, no.java.ems.domain.Event>rights(emsEvents).
            map(eventFromEms.f(configuration));

        return ok(rights(events));
    }

    public OperationResult<Event> getEventByName(String eventName) {
        Either<String, Event> eventOption = emsWrapper.
            findEventByName.f(eventName).
            right().bind(eventFromEms.f(configuration));

        return eventOption.either(OperationResult.<Event>notFound_(), OperationResult.<Event>ok_());
    }

    public OperationResult<List<Session>> getSessions(String eventName) {

        return emsWrapper.findEventByName.f(eventName).right().bind(eventFromEms.f(configuration)).
            either(OperationResult.<List<Session>>notFound_(), compose(OperationResult.<List<Session>>ok_(), getSessionsForEvent));
    }

    public OperationResult<Session> getSessionByTitle(final String eventName, final String sessionTitle) {
        OperationResult<Event> event = getEventByName(eventName);

        if (!event.isOk()) {
            return notFound("Event '" + eventName + "' not found.");
        }

        return OperationResult.joinOk(event.ok().map(new F<Event, OperationResult<Session>>() {
            public OperationResult<Session> f(Event event) {
                return emsWrapper.
                    findSessionIdsByEventIdAndTitle.f(event.id.toString()).f(sessionTitle).toEither(P.p("Could not find session with title '" + sessionTitle + "'.")).
                    right().bind(sessionFromEms.f(event)).
                    right().map(OperationResult.<Session>ok_()).
                    right().orValue(OperationResult.<Session>$notFound("Could not find session with title '" + sessionTitle + "' not found."));
            }
        }));
    }

    public OperationResult<Session> getSession(String eventName, final SessionId sessionId) {
        Either<String, Session> x = joinRight(emsWrapper.findEventByName.f(eventName).right().bind(eventFromEms.f(configuration)).
                right().map(new F<Event, Either<String, Session>>() {
            public Either<String, Session> f(Event event) {
                return emsWrapper.getSessionById.f(sessionId.value).
                    right().bind(sessionFromEms.f(event));
            }
        }));
        return OperationResult.fromEither(x);
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

    private OperationResult<Schedule> getSchedule_(String eventName, final Option<User> user) {
        return OperationResult.fromEither(emsWrapper.findEventByName.f(eventName).right().bind(eventFromEms.f(configuration)).
                right().map(createSchedule.f(user)));
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

    public OperationResult<byte[]> getSpeakerPhotoForSession(String sessionId, int index) {
        Either<String, no.java.ems.domain.Session> sessionOption = emsWrapper.getSessionById.f(sessionId);

        if(sessionOption.isLeft()) {
            return notFound(sessionOption.left().value());
        }

        no.java.ems.domain.Session session = sessionOption.right().value();

        Either<String, Speaker> speakerEither = EmsWrapper.getSpeakerFromSession.f(index).f(session);

        if(speakerEither.isLeft()) {
            return notFound(speakerEither.left().value());
        }

        Speaker speaker = speakerEither.right().value();

        Either<String, Binary> binary = EmsWrapper.getPhotoFromSpeaker.f(speaker);

        if (binary.isLeft()) {
            binary = emsWrapper.getPersonPhoto.f(speaker.getPersonId());
        }

        F<Either<Exception, byte[]>, Either<String, byte[]>> toString = Either.<Exception, byte[], String>leftMap_().f(Bottom.<Exception>eMessage());

        Either<String, byte[]> result = joinRight(binary.right().map(compose(toString, P1.<Either<Exception, byte[]>>__1(), Callables.<byte[]>either(), EmsWrapper.fetchBinary)));

        return result.either(OperationResult.<byte[]>notFound_(), OperationResult.<byte[]>ok_());
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    F<Event, List<Session>> getSessionsForEvent = new F<Event, List<Session>>() {
        public List<Session> f(Event event) {
            return Either.rights(emsWrapper.findSessionsByEventId.f(event.id.toString()).
                map(sessionFromEms.f(event)));
        }
    };

    F<Option<User>, F<Event, Schedule>> createSchedule = curry(new F2<Option<User>, Event, Schedule>() {
        public Schedule f(Option<User> userOption, Event event) {
            return new Schedule(event, getSessionsForEvent.f(event),
                    userOption.map(User.sessionAssociations_).orSome(User.emptySessionAssociations));
        }
    });
}
