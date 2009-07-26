package no.java.incogito.domain;

import fj.F;
import fj.F3;
import static fj.Function.curry;
import fj.data.Option;
import fj.data.TreeMap;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class User {
    public final UserId id;
    public final TreeMap<SessionId, UserSessionAssociation> sessionAssociations;
    /**
     * If none, this is a new object.
     */
    public final Option<User> original;

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private User(UserId id, TreeMap<SessionId, UserSessionAssociation> sessionAssociations, Option<User> original) {
        this.id = id;
        this.sessionAssociations = sessionAssociations;
        this.original = original;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public static User createPristineUser(UserId id) {
        return new User(id, TreeMap.<SessionId, UserSessionAssociation>empty(SessionId.ord), Option.<User>none());
    }

    public static User createPersistentUser(UserId id, TreeMap<SessionId, UserSessionAssociation> sessionAssociations) {
        return new User(id, sessionAssociations, Option.<User>none());
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public User setInterestLevel(SessionId sessionId, InterestLevel interestLevel) {
        UserSessionAssociation userSessionAssociation = sessionAssociations.get(sessionId).
                orSome(UserSessionAssociation.$constructor_().f(sessionId).f(interestLevel)).
                interestLevel(interestLevel);

        return new User(id, sessionAssociations.set(sessionId, userSessionAssociation), original);
    }

    // -----------------------------------------------------------------------
    // Higher-order functions
    // -----------------------------------------------------------------------

    public static final F<SessionId, F<InterestLevel, F<User, User>>> setInterestLevel = curry( new F3<SessionId, InterestLevel, User, User>() {
        public User f(SessionId sessionId, InterestLevel interestLevel, User user) {
            return user.setInterestLevel(sessionId, interestLevel);
        }
    });

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

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
