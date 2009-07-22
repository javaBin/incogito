package no.java.incogito.domain;

import fj.data.Option;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AttendanceMarker {
    public final SessionId sessionId;

    protected AttendanceMarker(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    public static AttendingMarker createAttendance(SessionId sessionId) {
        return new AttendingMarker(sessionId, Option.<SessionRating>none(), Option.<String>none());
    }

    public static AttendingMarker createAttendance(SessionId sessionId, Option<SessionRating> rating, Option<String> ratingComment) {
        return new AttendingMarker(sessionId, rating, ratingComment);
    }

    public static AttendanceMarker createInterest(SessionId sessionId) {
        return new InterestMarker(sessionId);
    }
}
