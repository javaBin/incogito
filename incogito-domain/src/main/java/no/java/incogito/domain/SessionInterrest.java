package no.java.incogito.domain;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SessionInterrest extends SessionAssociation {
    protected SessionInterrest(SessionId sessionId) {
        super(sessionId);
    }

    public static SessionInterrest createInterest(SessionId sessionId) {
        return new SessionInterrest(sessionId);
    }
}
