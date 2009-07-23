package no.java.incogito.domain;

import fj.F;
import fj.data.Option;
import fj.data.Set;
import static no.java.incogito.domain.AttendanceMarker.createAttendance;
import static no.java.incogito.domain.AttendanceMarker.createInterest;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class User {
    public final UserId id;
    public final Set<AttendanceMarker> attendanceMarkers;
    public final Option<User> original;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private User(UserId id, Set<AttendanceMarker> attendanceMarkers, Option<User> original) {
        this.id = id;
        this.attendanceMarkers = attendanceMarkers;
        this.original = original;
    }

    private User(UserId id, Set<AttendanceMarker> attendanceMarkers, boolean persistent) {
        this.id = id;
        this.attendanceMarkers = attendanceMarkers;
        this.original = persistent ? Option.some(this) : Option.<User>none();
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static User createTransientUser(UserId id) {
        return new User(id, Set.empty(AttendanceMarker.ord), false);
    }

    public static User createPersistentUser(UserId id, Set<AttendanceMarker> sessionAssociations) {
        return new User(id, sessionAssociations, true);
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public User markAttendance(SessionId session) {
        return new User(id, attendanceMarkers.insert(createAttendance(session)), original);
    }

    public User markInterest(SessionId session) {
        return new User(id, attendanceMarkers.insert(createInterest(session)), original);
    }

    /**
     * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
     * @version $Id$
     */
    public static class UserId {
        public final String value;

        public UserId(String value) {
            this.value = value;
        }

        public static UserId fromString(String value) {
            return new UserId(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserId userId = (UserId) o;

            return value.equals(userId.value);

        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        public static final F<String, UserId> fromString = new F<String, UserId>() {
            public UserId f(String value) {
                return fromString(value);
            }
        };
    }
}
