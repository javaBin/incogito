package no.java.incogito.domain;

import fj.data.List;
import fj.F;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Schedule {
    public final Event event;
    public final List<Session> sessions;
    public final List<AttendanceMarker> attendanceMarkers;

    public Schedule(Event event, List<Session> sessions, List<AttendanceMarker> attendanceMarkers) {
        this.event = event;
        this.sessions = sessions;
        this.attendanceMarkers = attendanceMarkers;
    }

    public List<Session> getAttendingSessions() {
        return sessions.filter(new F<Session, Boolean>() {
            public Boolean f(final Session session) {
                return attendanceMarkers.find(new F<AttendanceMarker, Boolean>() {
                    public Boolean f(AttendanceMarker attendanceMarker) {
                        return attendanceMarker.sessionId.equals(session.id);
                    }
                }).isSome();
            }
        });
    }
}
