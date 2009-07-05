package no.java.incogito.domain;

import fj.data.List;
import fj.data.Option;
import static no.java.incogito.domain.AttendanceMarker.createInterest;
import static no.java.incogito.domain.AttendanceMarker.createAttendance;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class User {
    public final UserId id;
    public final List<AttendanceMarker> attendanceMarkers;
    public final Option<User> original;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public User(UserId id, List<AttendanceMarker> attendanceMarkers, Option<User> original) {
        this.id = id;
        this.attendanceMarkers = attendanceMarkers;
        this.original = original;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private User(UserId id, List<AttendanceMarker> attendanceMarkers, boolean persistent) {
        this.id = id;
        this.attendanceMarkers = attendanceMarkers;
        this.original = persistent ? Option.some(this) : Option.<User>none();
    }

    public static User createTransientUser(UserId id) {
        return new User(id, List.<AttendanceMarker>nil(), false);
    }

    public static User createPersistentUser(UserId id, List<AttendanceMarker> sessionAssociations) {
        return new User(id, sessionAssociations, true);
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public User markAttendance(SessionId session) {
        return new User(id, attendanceMarkers.cons(createAttendance(session)), original);
    }

    public User markInterest(SessionId session) {
        return new User(id, attendanceMarkers.cons(createInterest(session)), original);
    }
}
