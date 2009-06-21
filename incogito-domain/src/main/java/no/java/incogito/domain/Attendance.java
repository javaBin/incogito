package no.java.incogito.domain;

import fj.data.Option;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Attendance extends SessionAssociation {
    public final Option<SessionRating> rating;
    public final Option<String> ratingComment;

    Attendance(SessionId sessionId, Option<SessionRating> rating, Option<String> ratingComment) {
        super(sessionId);
        this.rating = rating;
        this.ratingComment = ratingComment;
    }

    public static Attendance createAttendance(SessionId sessionId) {
        return new Attendance(sessionId, Option.<SessionRating>none(), Option.<String>none());
    }

    public static Attendance createInterrest(SessionId sessionId) {
        return new Attendance(sessionId, Option.<SessionRating>none(), Option.<String>none());
    }

    public static Attendance createAttendance(SessionId sessionId, Option<SessionRating> rating, Option<String> ratingComment) {
        return new Attendance(sessionId, rating, ratingComment);
    }
}
