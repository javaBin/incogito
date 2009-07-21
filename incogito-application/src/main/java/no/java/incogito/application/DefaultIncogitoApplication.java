package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.F2;
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
import no.java.ems.client.EventsClient;
import no.java.ems.client.SessionsClient;
import no.java.ems.service.EmsService;
import no.java.incogito.Functions;
import no.java.incogito.domain.AttendanceMarker;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserId;
import no.java.incogito.ems.client.EmsFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component("incogitoApplication")
public class DefaultIncogitoApplication implements IncogitoApplication {
    private final UserClient userClient;
    private final EventsClient eventsClient;

    private final SessionsClient sessionsClient;

    @Autowired
    public DefaultIncogitoApplication(UserClient userClient, EmsService emsService) {
        this.userClient = userClient;
        this.eventsClient = emsService.getEventsClient();
        this.sessionsClient = emsService.getSessionsClient();
    }

    public OperationResult<List<Event>> getEvents() {
        return OperationResult.ok(iterableList(eventsClient.listEvents()).map(eventFromEms));
    }

//    public OperationResult<Event> getEvent(Event.EventId eventId) {
//        return fromNull(eventsClient.get(eventId.toString())).
//                map(Function.compose(OperationResult.<Event>ok_(), eventFromEms)).
//                orSome(OperationResult.<Event>notFound("Event with id '" + eventId + "' not found."));
//    }

    public OperationResult<Event> getEventByName(String eventName) {
        return findEmsEventByName.f(eventName).
                map(compose(OperationResult.<Event>ok_(), eventFromEms)).
                orSome(OperationResult.<Event>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<List<Session>> getSessions(String eventName) {
        F<no.java.ems.domain.Event, OperationResult<List<Session>>> f = Functions.compose(
                OperationResult.<List<Session>>ok_(),
                this.<Session>filterAndRemove(),
                List.<no.java.ems.domain.Session, Option<Session>>map_().f(sessionFromEms),
                List.<String, no.java.ems.domain.Session>map_().f(getSession),
                findSessionIdsByEvent,
                compose(Event.getId, eventFromEms));

        return iterableList(eventsClient.listEvents()).
                find(compose(Functions.equals.f(eventName), EmsFunctions.eventName)).
                map(f).
                orSome(OperationResult.<List<Session>>notFound("Event with name '" + eventName + "' not found."));
    }

    public OperationResult<Session> getSession(String eventName, String sessionTitle) {

        F<no.java.ems.domain.Event, Option<Session>> f = Functions.compose(
                Functions.<Session>Option_join_(),
                Functions.<String, Option<Session>>Option_map(compose(sessionFromEms, getSession)),
                Functions.<String>toOption_(),
                flip(findSessionIdsByTitle).f(sessionTitle),
                EmsFunctions.eventId);

        return join(iterableList(eventsClient.listEvents()).
                find(compose(Functions.equals.f(eventName), EmsFunctions.eventName)).
                map(f)).
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

    public OperationResult<Schedule> getSchedule(UserId id) {
        return userClient.getUser(id).map(new F<User, Schedule>() {
            public Schedule f(User user) {
                return new Schedule();
            }
        }).map(OperationResult.<Schedule>ok_()).
                orSome(OperationResult.<Schedule>$notFound("User '" + id.value + "' not found."));
    }

    public OperationResult markAttendance(UserId userId, final SessionId sessionId, AttendanceMarker attendanceMarker) {

        Option<User> option = userClient.getUser(userId);

        option.foreach(new Effect<User>() {
            public void e(User user) {
                user = user.markAttendance(sessionId);
                userClient.setUser(user);
            }
        });

        return option.map(OperationResult.<User>ok_()).
                orSome(OperationResult.<User>$notFound("User '" + userId.value + "' not found."));
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private F<String, no.java.ems.domain.Session> getSession = new F<String, no.java.ems.domain.Session>() {
        public no.java.ems.domain.Session f(String id) {
            return sessionsClient.get(id);
        }
    };

    public F<String, Option<no.java.ems.domain.Event>> findEmsEventByName = new F<String, Option<no.java.ems.domain.Event>>() {
        public Option<no.java.ems.domain.Event> f(String eventName) {
            return iterableList(eventsClient.listEvents()).
                    find(compose(Functions.equals.f(eventName), EmsFunctions.eventName));
        }
    };

    public F<Event.EventId, List<String>> findSessionIdsByEvent = new F<EventId, List<String>>() {
        public List<String> f(EventId eventId) {
            return iterableList(sessionsClient.findSessionIdsByEvent(eventId.value.toString()));
        }
    };

    public F<String, F<String, List<String>>> findSessionIdsByTitle = curry(new F2<String, String, List<String>>() {
        public List<String> f(String eventId, String title) {
            return iterableList(sessionsClient.findSessionsByTitle(eventId, title));
        }
    });

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

            return some(new Session(Session.id(session.getId()),
                    session.getTitle(),
                    fromNull(session.getTimeslot()),
                    fromNull(session.getRoom()).map(EmsFunctions.roomName),
                    iterableList(session.getTags()),
                    iterableList(session.getSpeakers()).map(speakerFromEms),
                    List.<Comment>nil()));
        }
    };

    <A> F<List<Option<A>>, List<A>> filterAndRemove() {
        return Function.compose(
            List.<Option<A>, A>map_().f(Functions.<A>Option_somes()),
            Functions.<Option<A>>List_filter().f(Option.<A>isSome_()));
    }
}
