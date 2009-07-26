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
import static fj.data.Option.join;
import static fj.data.Option.none;
import static fj.data.Option.some;
import no.java.incogito.Functions;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import no.java.incogito.ems.client.EmsFunctions;
import no.java.incogito.ems.client.EmsWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component("incogitoApplication")
public class DefaultIncogitoApplication implements IncogitoApplication {
    private final UserClient userClient;
    private final EmsWrapper emsWrapper;

    @Autowired
    public DefaultIncogitoApplication(UserClient userClient, EmsWrapper emsWrapper) {
        this.userClient = userClient;
        this.emsWrapper = emsWrapper;
    }

    public OperationResult<List<Event>> getEvents() {
        return OperationResult.ok(emsWrapper.listEvents().map(eventFromEms));
    }

    public OperationResult<Event> getEventByName(String eventName) {
        return emsWrapper.findEventByName.f(eventName).
                map(compose(OperationResult.<Event>ok_(), eventFromEms)).
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

    public OperationResult<Session> getSession(String eventName, String sessionTitle) {
        return findSession().f(eventName).f(sessionTitle).
                map(OperationResult.<Session>ok_()).
                orSome(OperationResult.<Session>notFound("Could not find session with title '" + sessionTitle + "' not found."));
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

        Option<no.java.ems.domain.Event> emsEvent = emsWrapper.findEventByName.f(eventName);

        F<no.java.ems.domain.Event, List<Session>> f = Functions.compose(
                this.<Session>filterAndRemove(),
                List.<no.java.ems.domain.Session, Option<Session>>map_().f(sessionFromEms),
                List.<String, no.java.ems.domain.Session>map_().f(emsWrapper.getSessionById),
                emsWrapper.findSessionIdsByEventId,
                EmsFunctions.eventId);

        Option<Event> event = emsEvent.map(eventFromEms);
        Option<List<Session>> sessions = emsEvent.map(f);
        Option<User> user = userClient.getUser(new UserId(userId));

        return user.bind(sessions, event, createSchedule).
                map(OperationResult.<Schedule>ok_()).
                orSome(OperationResult.<Schedule>$notFound("User '" + userId + "' not found."));
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

    F<no.java.ems.domain.Event, Event> eventFromEms = new F<no.java.ems.domain.Event, Event>() {
        public Event f(no.java.ems.domain.Event event) {
            return new Event(Event.id(event.getId()), event.getName());
        }
    };

    F<no.java.ems.domain.Speaker, Speaker> speakerFromEms = new F<no.java.ems.domain.Speaker, Speaker>() {
        public Speaker f(no.java.ems.domain.Speaker speaker) {
            return new Speaker(speaker.getName(), speaker.getDescription());
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

            return some(new Session(Session.id(session.getId()),
                    session.getTitle(),
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
            this.<Session>filterAndRemove(),
            List.<no.java.ems.domain.Session, Option<Session>>map_().f(sessionFromEms),
            List.<String, no.java.ems.domain.Session>map_().f(emsWrapper.getSessionById),
            emsWrapper.findSessionIdsByEventId,
            EmsFunctions.eventId);
    }

    private F<String, F<String, Option<Session>>> findSession() {
        return curry(new F2<String, String, Option<Session>>() {
            public Option<Session> f(final String eventName, final String sessionTitle) {
                F<no.java.ems.domain.Event, Option<Session>> f = Functions.compose(
                        Functions.<Session>Option_join_(),
                        Functions.<String, Option<Session>>Option_map(compose(sessionFromEms, emsWrapper.getSessionById)),
                        Functions.<String>toOption_(),
                        flip(emsWrapper.findSessionIdsByEventIdAndTitle).f(sessionTitle),
                        EmsFunctions.eventId);
                return join(emsWrapper.findEventByName.f(eventName).map(f));
            }
        });
    }

    F<User, F<List<Session>, F<Event, Schedule>>> createSchedule = curry( new F3<User, List<Session>, Event, Schedule>() {
        public Schedule f(User user, List<Session> sessions, Event event) {
            return new Schedule(event, sessions, user.sessionAssociations);
        }
    });

    <A> F<List<Option<A>>, List<A>> filterAndRemove() {
        return Function.compose(
            List.<Option<A>, A>map_().f(Functions.<A>Option_somes()),
            Functions.<Option<A>>List_filter().f(Option.<A>isSome_()));
    }
}
