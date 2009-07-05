package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.Function;
import fj.P1;
import fj.data.Either;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import no.java.ems.client.EventsClient;
import no.java.ems.client.SessionsClient;
import no.java.ems.service.EmsService;
import no.java.incogito.domain.AttendanceMarker;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
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

    private F<String, no.java.ems.domain.Session> getSession = new F<String, no.java.ems.domain.Session>() {
        public no.java.ems.domain.Session f(String id) {
            return sessionsClient.get(id);
        }
    };

    private final F<no.java.ems.domain.Session, Session> toSession = new F<no.java.ems.domain.Session, Session>() {
        public Session f(no.java.ems.domain.Session session) {
            return new Session(new SessionId(session.getId()), List.<Comment>nil());
        }
    };

    public List<Event> getEvents() {
        return iterableList(eventsClient.listEvents()).map(new F<no.java.ems.domain.Event, Event>() {
            public Event f(no.java.ems.domain.Event event) {
                return new Event(Event.id(event.getId()), event.getName());
            }
        });
    }

    public List<P1<Session>> getSessions(final Event.EventId eventId) {

        F<String, P1<Session>> f = P1.curry(Function.compose(toSession, getSession));

//        Strategy<Session> strategy = Strategy.simpleThreadStrategy();
//        P1<List<Session>> listP1 = strategy.parList(sessions);

        return findSessionIdsByEvent(eventId)._1().map(f);
    }

    public P1<List<String>> findSessionIdsByEvent(final Event.EventId eventId){
        return new P1<List<String>>() {
            public List<String> _1() {
                return iterableList(sessionsClient.findSessionIdsByEvent(eventId.value.toString()));
            }
        };
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

    public OperationResult<User> removeUser(UserId userId) {
        Option<User> user = userClient.getUser(userId);

        user.foreach(new Effect<User>() {
            public void e(User user) {
                userClient.setUser(user);
            }
        });

        return user.map(OperationResult.<User>ok_()).
                orSome(OperationResult.<User>$conflict("User with id '" + userId.value + "' already exist."));
    }

    public Option<User> getUser(UserId userId) {
        return userClient.getUser(userId);
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
}
