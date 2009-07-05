package no.java.incogito.domain;

import fj.data.Option;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AttendingMarker extends AttendanceMarker {
    public final Option<SessionRating> rating;
    public final Option<String> ratingComment;

    AttendingMarker(SessionId sessionId, Option<SessionRating> rating, Option<String> ratingComment) {
        super(sessionId);
        this.rating = rating;
        this.ratingComment = ratingComment;
    }
}
