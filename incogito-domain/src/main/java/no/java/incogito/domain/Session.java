package no.java.incogito.domain;

import fj.data.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Session {
    public final SessionId id;
    public final String title;
    public final List<Comment> comments;

    public Session(SessionId id, String title, List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.comments = comments;
    }

// -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static SessionId id(String value) {
        return new SessionId(value);
    }
}
