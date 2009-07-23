package no.java.incogito.domain;

import fj.F;
import fj.data.List;
import fj.data.Set;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Schedule {
    public final Event event;
    public final List<Session> sessions;
    public final Set<AttendanceMarker> attendanceMarkers;

    public Schedule(Event event, List<Session> sessions, Set<AttendanceMarker> attendanceMarkers) {
        this.event = event;
        this.sessions = sessions;
        this.attendanceMarkers = attendanceMarkers;
    }

    public List<Session> getAttendingSessions() {
        return sessions.filter(new F<Session, Boolean>() {
            public Boolean f(final Session session) {
                for (AttendanceMarker marker : attendanceMarkers) {
                    if (SessionId.ord.eq(session.id, marker.sessionId)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
