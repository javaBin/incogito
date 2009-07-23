package no.java.incogito.domain;

import fj.data.Option;
import fj.pre.Ord;
import fj.pre.Ordering;
import fj.Function;
import fj.F2;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AttendanceMarker {

    public final static Ord<AttendanceMarker> ord = Ord.ord(Function.curry( new F2<AttendanceMarker, AttendanceMarker, Ordering>() {
        public Ordering f(AttendanceMarker a, AttendanceMarker b) {
            return SessionId.ord.compare(a.sessionId, b.sessionId);
        }
    }));

    public final SessionId sessionId;

    protected AttendanceMarker(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    // -----------------------------------------------------------------------
    // Attending
    // -----------------------------------------------------------------------

    public static class AttendingMarker extends AttendanceMarker {
        public final Option<SessionRating> rating;
        public final Option<String> ratingComment;

        AttendingMarker(SessionId sessionId, Option<SessionRating> rating, Option<String> ratingComment) {
            super(sessionId);
            this.rating = rating;
            this.ratingComment = ratingComment;
        }
    }

    public static AttendanceMarker createAttendance(SessionId sessionId) {
        return new AttendingMarker(sessionId, Option.<SessionRating>none(), Option.<String>none());
    }

    public static AttendanceMarker createAttendance(SessionId sessionId, Option<SessionRating> rating, Option<String> ratingComment) {
        return new AttendingMarker(sessionId, rating, ratingComment);
    }

    // -----------------------------------------------------------------------
    // Interest
    // -----------------------------------------------------------------------

    public static class InterestMarker extends AttendanceMarker {
        protected InterestMarker(SessionId sessionId) {
            super(sessionId);
        }
    }

    public static AttendanceMarker createInterest(SessionId sessionId) {
        return new InterestMarker(sessionId);
    }

    // -----------------------------------------------------------------------
    // No Attendance
    // -----------------------------------------------------------------------

    public static class NoAttendanceMarker extends AttendanceMarker {
        protected NoAttendanceMarker(SessionId sessionId) {
            super(sessionId);
        }
    }

    public static AttendanceMarker createNoAttendance(SessionId sessionId) {
        return new NoAttendanceMarker(sessionId);
    }
}
