package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.F2;
import fj.data.Java;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.fromNull;
import fj.data.TreeMap;
import fj.pre.Ord;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.SessionRating;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import static no.java.incogito.domain.User.createPersistentUser;
import no.java.incogito.domain.UserSessionAssociation;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;
import no.java.incogito.voldemort.VoldemortF;
import voldemort.client.StoreClient;
import voldemort.client.UpdateAction;
import voldemort.versioning.Versioned;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UserClient {

    public static final String SCHEMA_ID = "id";

    public static final String SCHEMA_SESSION_ASSOCIATIONS = "sessionAssociations";

    public static final String SCHEMA_SESSION_ID = "sessionId";

    public static final String SCHEMA_INTEREST_LEVEL = "interestLevel";

    public static final String SCHEMA_RATING = "rating";

    public static final String SCHEMA_RATING_COMMENT = "ratingComment";

    public static final String SCHEMA = "{" +
            "\"" + SCHEMA_ID + "\":\"string\", " +
            "\"" + SCHEMA_SESSION_ASSOCIATIONS + "\":[{" +
            "   \"" + SCHEMA_SESSION_ID + "\":\"string\", " +
            "   \"" + SCHEMA_INTEREST_LEVEL + "\":\"string\", " +
            "   \"" + SCHEMA_RATING + "\":\"string\", " +
            "   \"" + SCHEMA_RATING_COMMENT + "\":\"string\"}]" +
            "}";

    private final StoreClient<String, Map> client;

    public UserClient(StoreClient<String, Map> client) {
        this.client = client;
    }

    public Option<User> getUser(no.java.incogito.domain.User.UserId id) {
        Option<Versioned<Map>> option = fromNull(client.get(id.value));

        return option.map(VoldemortF.<Map>verionedGetValue()).map(fromMap);
    }

    public void setUser(final User user) {
        UpdateAction<String, Map> updateAction = new UpdateAction<String, Map>() {
            public void update(StoreClient<String, Map> client) {
                client.put(user.id.value, toMap.f(user));
            }
        };

        if (user.original.isNone()) {
            Map value = toMap.f(user);
            client.put(user.id.value, value);
        } else {
            if (!client.applyUpdate(updateAction)) {
                throw new RuntimeException("Could not apply update.");
            }
        }
    }

    public boolean removeUser(final no.java.incogito.domain.User.UserId id) {
        return client.delete(id.value);
    }

    // -----------------------------------------------------------------------
    // First-Order Functions
    // -----------------------------------------------------------------------

    public final Effect<User> setUser = new Effect<User>() {
        public void e(User user) {
            setUser(user);
        }
    };

    public final F<UserId, Option<User>> getUser = new F<UserId, Option<User>>() {
        public Option<User> f(UserId userId) {
            return getUser(userId);
        }
    };
    
    // -----------------------------------------------------------------------
    // Utility Functions
    // -----------------------------------------------------------------------

    private static final F<Map, User> fromMap = new F<Map, User>() {
        public User f(Map map) {
            //noinspection unchecked

            List<Map> list = List.iterableList(Option.fromNull((java.util.List<Map>) map.get(SCHEMA_SESSION_ASSOCIATIONS)).orSome(Collections.<Map>emptyList()));

            F2<TreeMap<SessionId, UserSessionAssociation>, UserSessionAssociation, TreeMap<SessionId, UserSessionAssociation>> folder = new F2<TreeMap<SessionId, UserSessionAssociation>, UserSessionAssociation, TreeMap<SessionId, UserSessionAssociation>>() {
                public TreeMap<SessionId, UserSessionAssociation> f(TreeMap<SessionId, UserSessionAssociation> sessionAssociations, UserSessionAssociation sessionAssociation) {
                    return sessionAssociations.set(sessionAssociation.sessionId, sessionAssociation);
                }
            };

            TreeMap<SessionId, UserSessionAssociation> sessionAssociations = Option.somes(list.map(sessionAssociationFromMap)).
                    foldLeft(folder, User.emptySessionAssociations);

            return createPersistentUser(UserId.userId.f(map.get(SCHEMA_ID).toString()), sessionAssociations);
        }
    };

    private static final F<User, Map> toMap = new F<User, Map>() {
        public Map f(final User user) {
            final List<Map> attendanceMarkers = user.sessionAssociations.values().map(sessionAssociationToMap);

            return new HashMap<String, Object>() {{
                put(SCHEMA_ID, user.id.value);
                put(SCHEMA_SESSION_ASSOCIATIONS, Java.<Map>List_ArrayList().f(attendanceMarkers));
            }};
        }
    };

    private static F<UserSessionAssociation, Map> sessionAssociationToMap = new F<UserSessionAssociation, Map>() {
        public Map f(final UserSessionAssociation sessionAssociation) {
            return new HashMap<String, Object>() {{
                put(SCHEMA_SESSION_ID, sessionAssociation.sessionId.value);
                put(SCHEMA_INTEREST_LEVEL, sessionAssociation.interestLevel.toString());
                put(SCHEMA_RATING, sessionAssociation.rating.map(sessionRatingToString).orSome((String) null));
                put(SCHEMA_RATING_COMMENT, sessionAssociation.ratingComment.orSome((String) null));
            }};
        }
    };

    private static F<Map, Option<UserSessionAssociation>> sessionAssociationFromMap = new F<Map, Option<UserSessionAssociation>>() {
        public Option<UserSessionAssociation> f(Map m) {
            @SuppressWarnings({"unchecked"})
            TreeMap<String, String> map = TreeMap.<String, String>fromMutableMap(Ord.stringOrd, m);

            Option<SessionId> sessionId = map.get(SCHEMA_SESSION_ID).bind(Option.fromString()).map(SessionId.fromString);

            Option<SessionRating> rating = map.get(SCHEMA_RATING).bind(Option.fromString()).map(sessionRatingFromString);
            Option<String> ratingComment = map.get(SCHEMA_RATING_COMMENT).bind(Option.fromString());

            Option<InterestLevel> interestLevel = map.get(SCHEMA_INTEREST_LEVEL).
                    bind(InterestLevel.valueOf_);

            return sessionId.bind(interestLevel, UserSessionAssociation.constructor_).
                    map(UserSessionAssociation.rating_.f(rating)).
                    map(UserSessionAssociation.ratingComment_.f(ratingComment));
        }
    };

    private static F<SessionRating, String> sessionRatingToString = new F<SessionRating, String>() {
        public String f(SessionRating sessionRating) {
            return sessionRating.name();
        }
    };

    private static F<String, SessionRating> sessionRatingFromString = new F<String, SessionRating>() {
        public SessionRating f(String s) {
            return SessionRating.valueOf(s);
        }
    };
}
