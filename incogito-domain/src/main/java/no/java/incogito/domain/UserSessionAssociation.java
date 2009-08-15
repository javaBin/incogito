package no.java.incogito.domain;

import fj.F;
import fj.P1;
import fj.F2;
import static fj.Function.curry;
import fj.data.Option;
import no.java.incogito.Enums;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UserSessionAssociation {

    public enum InterestLevel {
        ATTEND,
        INTEREST,
        NO_INTEREST;

        public static final F<String, Option<InterestLevel>> valueOf_ = Enums.<InterestLevel>valueOf().f(InterestLevel.class);
    }

    public final SessionId sessionId;
    public final InterestLevel interestLevel;
    public final Option<SessionRating> rating;
    public final Option<String> ratingComment;

    public UserSessionAssociation(SessionId sessionId, InterestLevel interestLevel, Option<SessionRating> rating, Option<String> ratingComment) {
        this.sessionId = sessionId;
        this.interestLevel = interestLevel;
        this.rating = rating;
        this.ratingComment = ratingComment;
    }

    public static UserSessionAssociation constructor(SessionId sessionId, InterestLevel interestLevel, Option<SessionRating> rating, Option<String> ratingComment) {
        return new UserSessionAssociation(sessionId, interestLevel, rating, ratingComment);
    }

    public static UserSessionAssociation constructor(SessionId sessionId, InterestLevel interestLevel) {
        return new UserSessionAssociation(sessionId, interestLevel, Option.<SessionRating>none(), Option.<String>none());
    }

    public static final F<SessionId, F<InterestLevel, UserSessionAssociation>> constructor_ = curry( new F2<SessionId, InterestLevel, UserSessionAssociation>() {
        public UserSessionAssociation f(SessionId sessionId, InterestLevel interestLevel) {
            return constructor(sessionId, interestLevel);
        }
    });

    public static F<SessionId, F<InterestLevel, P1<UserSessionAssociation>>> $constructor_() {
        return new F<SessionId, F<InterestLevel, P1<UserSessionAssociation>>>() {
            public F<InterestLevel, P1<UserSessionAssociation>> f(final SessionId sessionId) {
                return P1.curry(new F<InterestLevel, UserSessionAssociation>() {
                    public UserSessionAssociation f(InterestLevel interestLevel) {
                        return constructor(sessionId, interestLevel);
                    }
                });
            }
        };
    }

    public static final F<Option<SessionRating>, F<UserSessionAssociation, UserSessionAssociation>> rating_ = curry(new F2<Option<SessionRating>, UserSessionAssociation, UserSessionAssociation>() {
        public UserSessionAssociation f(Option<SessionRating> rating, UserSessionAssociation userSessionAssociation) {
            return userSessionAssociation.rating(rating);
        }
    });

    public static final F<Option<String>, F<UserSessionAssociation, UserSessionAssociation>> ratingComment_ = curry(new F2<Option<String>, UserSessionAssociation, UserSessionAssociation>() {
        public UserSessionAssociation f(Option<String> ratingComment, UserSessionAssociation userSessionAssociation) {
            return userSessionAssociation.ratingComment(ratingComment);
        }
    });

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    public UserSessionAssociation rating(Option<SessionRating> rating) {
        return new UserSessionAssociation(sessionId, interestLevel, rating, ratingComment);
    }

    public UserSessionAssociation ratingComment(Option<String> ratingComment) {
        return new UserSessionAssociation(sessionId, interestLevel, rating, ratingComment);
    }

    public UserSessionAssociation interestLevel(InterestLevel interestLevel) {
        return new UserSessionAssociation(sessionId, interestLevel, rating, ratingComment);
    }
}
