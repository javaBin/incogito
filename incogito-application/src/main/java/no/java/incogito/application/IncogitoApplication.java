package no.java.incogito.application;

import fj.P1;
import fj.Unit;
import fj.data.List;
import no.java.incogito.domain.AttendanceMarker;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserId;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface IncogitoApplication {
    OperationResult<List<Event>> getEvents();

    OperationResult<Event> getEventByName(String eventName);

    OperationResult<List<Session>> getSessions(String eventName);

    OperationResult<Session> getSession(String eventName, String sessionTitle);

    OperationResult<User> createUser(User user);

    OperationResult<Unit> removeUser(UserId userId);

    OperationResult<User> getUser(UserId userId);

    OperationResult<Schedule> getSchedule(UserId id);

    OperationResult markAttendance(UserId userId, SessionId session, AttendanceMarker attendanceMarker);
}
