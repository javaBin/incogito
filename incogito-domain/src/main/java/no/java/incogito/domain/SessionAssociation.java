package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class SessionAssociation {
    public final SessionId sessionId;

    protected SessionAssociation(SessionId sessionId) {
        this.sessionId = sessionId;
    }
}
