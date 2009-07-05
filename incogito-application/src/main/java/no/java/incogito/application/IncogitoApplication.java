package no.java.incogito.application;

import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.UserId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.AttendanceMarker;
import fj.data.Option;
import fj.data.List;
import fj.P1;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface IncogitoApplication {
    List<Event> getEvents();

    List<P1<Session>> getSessions(Event.EventId eventId);

    OperationResult<User> createUser(User user);

    OperationResult<User> removeUser(UserId userId);

    Option<User> getUser(UserId userId);

    OperationResult<Schedule> getSchedule(UserId id);

    OperationResult markAttendance(UserId userId, SessionId session, AttendanceMarker attendanceMarker);
}
