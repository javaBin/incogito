package no.java.incogito.domain;

import fj.data.List;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Session {
    public final SessionId id;
    public final List<Comment> comments;

    public Session(SessionId id, List<Comment> comments) {
        this.id = id;
        this.comments = comments;
    }
}
