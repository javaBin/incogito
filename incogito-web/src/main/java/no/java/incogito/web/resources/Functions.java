package no.java.incogito.web.resources;

import fj.F;
import fj.F2;
import fj.P1;
import static fj.Function.curry;
import fj.data.List;
import no.java.incogito.domain.AttendanceMarker;
import no.java.incogito.domain.AttendingMarker;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.dto.AttendanceMarkerXml;
import static no.java.incogito.dto.AttendanceMarkerXml.State.ATTENDING;
import static no.java.incogito.dto.AttendanceMarkerXml.State.INTEREST;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionXml;

import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Functions {
    public static final F<Event, EventXml> eventToXml = new F<Event, EventXml>() {
        public EventXml f(Event event) {
            return new EventXml(event.name);
        }
    };

    public static final F<List<Event>, List<EventXml>> eventListToXml = List.<Event, EventXml>map_().f(Functions.eventToXml);

    public static final F<P1<UriBuilder>, F<Session, SessionXml>> sessionToXml = curry(new F2<P1<UriBuilder>, Session, SessionXml>() {
        public SessionXml f(P1<UriBuilder> uriBuilder, Session session) {
            return new SessionXml(uriBuilder._1().segment(session.title).build().toString(), session.id.value, session.title);
        }
    });

    private static F<AttendanceMarker, AttendanceMarkerXml> attendanceMarkerToXml = new F<AttendanceMarker, AttendanceMarkerXml>() {
        public AttendanceMarkerXml f(AttendanceMarker attendanceMarker) {
            return new AttendanceMarkerXml(attendanceMarker.sessionId.value,
                    attendanceMarker instanceof AttendingMarker ? ATTENDING : INTEREST);
        }
    };

    public static final F<P1<UriBuilder>, F<Schedule, ScheduleXml>> scheduleToXml = curry(new F2<P1<UriBuilder>, Schedule, ScheduleXml>() {
        public ScheduleXml f(P1<UriBuilder> uriBuilder, Schedule schedule) {
            return new ScheduleXml(schedule.sessions.map(sessionToXml.f(uriBuilder)),
                    schedule.attendanceMarkers.map(attendanceMarkerToXml));
        }
    });
}
