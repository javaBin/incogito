package no.java.incogito.web.resources;

import fj.Bottom;
import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P1;
import fj.data.List;
import no.java.incogito.domain.AttendanceMarker;
import no.java.incogito.domain.AttendanceMarker.InterestMarker;
import no.java.incogito.domain.AttendanceMarker.NoAttendanceMarker;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.dto.AttendanceMarkerXml;
import no.java.incogito.dto.AttendanceMarkerXml.State;
import static no.java.incogito.dto.AttendanceMarkerXml.State.ATTENDING;
import static no.java.incogito.dto.AttendanceMarkerXml.State.INTERESTED;
import static no.java.incogito.dto.AttendanceMarkerXml.State.NOT_ATTENDING;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionXml;

import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlFunctions {
    public static final F<Event, EventXml> eventToXml = new F<Event, EventXml>() {
        public EventXml f(Event event) {
            return new EventXml(event.name);
        }
    };

    public static final F<List<Event>, List<EventXml>> eventListToXml = List.<Event, EventXml>map_().f(XmlFunctions.eventToXml);

    public static final F<P1<UriBuilder>, F<Session, SessionXml>> sessionToXml = curry(new F2<P1<UriBuilder>, Session, SessionXml>() {
        public SessionXml f(P1<UriBuilder> uriBuilder, Session session) {
            return new SessionXml(uriBuilder._1().segment(session.title).build().toString(), session.id.value, session.title);
        }
    });

    private static final F<AttendanceMarker, AttendanceMarkerXml> attendanceMarkerToXml = new F<AttendanceMarker, AttendanceMarkerXml>() {
        public AttendanceMarkerXml f(AttendanceMarker attendanceMarker) {
            State state;
            if (attendanceMarker instanceof AttendanceMarker.AttendingMarker) {
                state = ATTENDING;
            } else if (attendanceMarker instanceof InterestMarker) {
                state = INTERESTED;
            } else if (attendanceMarker instanceof NoAttendanceMarker) {
                state = NOT_ATTENDING;
            } else {
                throw Bottom.error("Unknown attendance marker: " + attendanceMarker);
            }

            return new AttendanceMarkerXml(attendanceMarker.sessionId.value, state);
        }
    };

    public static final F<AttendanceMarkerXml, AttendanceMarker> attendanceMarkerFromXml = new F<AttendanceMarkerXml, AttendanceMarker>() {
        public AttendanceMarker f(AttendanceMarkerXml attendanceMarker) {
            SessionId sessionId = new SessionId(attendanceMarker.sessionId);

            switch (attendanceMarker.state) {
                case ATTENDING:
                    return AttendanceMarker.createAttendance(sessionId);
                case INTERESTED:
                    return AttendanceMarker.createInterest(sessionId);
                case NOT_ATTENDING:
                    return AttendanceMarker.createNoAttendance(sessionId);
                default:
                    throw Bottom.error("Unknown attendance type: " + attendanceMarker.state);
            }
        }
    };

    public static final F<P1<UriBuilder>, F<Schedule, ScheduleXml>> scheduleToXml = curry(new F2<P1<UriBuilder>, Schedule, ScheduleXml>() {
        public ScheduleXml f(P1<UriBuilder> uriBuilder, Schedule schedule) {
            return new ScheduleXml(schedule.sessions.map(sessionToXml.f(uriBuilder)),
                    schedule.attendanceMarkers.toList().map(attendanceMarkerToXml));
        }
    });
}
