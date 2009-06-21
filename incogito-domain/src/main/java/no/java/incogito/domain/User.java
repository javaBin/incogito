package no.java.incogito.domain;

import fj.data.List;
import fj.data.Option;
import static no.java.incogito.domain.Attendance.createAttendance;
import static no.java.incogito.domain.SessionInterrest.createInterest;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class User {
    public final UserId id;
    public final List<SessionAssociation> sessionAssociations;
    public final Option<User> original;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public User(UserId id, List<SessionAssociation> sessionAssociations, Option<User> original) {
        this.id = id;
        this.sessionAssociations = sessionAssociations;
        this.original = original;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private User(UserId id, List<SessionAssociation> sessionAssociations, boolean persistent) {
        this.id = id;
        this.sessionAssociations = sessionAssociations;
        this.original = persistent ? Option.some(this) : Option.<User>none();
    }

    public static User createTransientUser(UserId id) {
        return new User(id, List.<SessionAssociation>nil(), false);
    }

    public static User createPersistentUser(UserId id, List<SessionAssociation> sessionAssociations) {
        return new User(id, sessionAssociations, true);
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public User markAttendance(SessionId session) {
        return new User(id, sessionAssociations.cons(createAttendance(session)), original);
    }

    public User markInterest(SessionId session) {
        return new User(id, sessionAssociations.cons(createInterest(session)), original);
    }
}
