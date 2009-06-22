package no.java.incogito.application;

import fj.Effect;
import fj.F;
import fj.data.List;
import static fj.data.List.nil;
import fj.data.Option;
import static fj.data.Option.fromNull;
import no.java.incogito.domain.Attendance;
import no.java.incogito.domain.SessionAssociation;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.SessionInterrest;
import no.java.incogito.domain.SessionRating;
import no.java.incogito.domain.User;
import static no.java.incogito.domain.User.createPersistentUser;
import no.java.incogito.domain.UserId;
import no.java.incogito.voldemort.VoldemortF;
import static no.java.incogito.voldemort.VoldemortF.toNull;
import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UserClient {

    public static final String SCHEMA = "{" +
        "\"id\":\"string\", " +
        "\"sessionAssociations\":[{" +
        "   \"session\":\"string\", " +
        "   \"attending\":\"boolean\", " +
        "   \"rating\":\"int32\", " +
        "   \"ratingComment\":\"string\"}]" +
        "}";

    private final StoreClient<String, Map> client;

    public UserClient(StoreClient<String, Map> client) {
        this.client = client;
    }

    public Option<User> getUser(UserId id) {
        Option<Versioned<Map>> option = fromNull(client.get(id.value));

        return option.map(VoldemortF.<Map>verionedGetValue()).map(fromMap);
    }

    public void setUser(User user) {
        if (user.original.isNone()) {
            client.put(user.id.value, toMap.f(user));
        } else {
            throw new RuntimeException("Updates is not implemented");
        }
    }

    private static final F<Map, User> fromMap = new F<Map, User>() {
        public User f(Map map) {
            List<SessionAssociation> sessionAssociations = nil();

            for (Object o : (java.util.List) map.get("sessionAssociations")) {
                Map sessionAssociation = (Map) o;

                sessionAssociations = sessionAssociations.cons(sessionAssociationFromMap.f(sessionAssociation));
            }

            return createPersistentUser(UserId.fromString.f(map.get("id").toString()), sessionAssociations.reverse());
        }
    };

    private static final F<User, Map> toMap = new F<User, Map>() {
        public Map f(final User user) {
            final List<Map> sessionAssociations = user.sessionAssociations.map(sessionAssociationToMap);

            return new HashMap<String, Object>() {{
                put("id", user.id.value);
                put("sessionAssociations", new ArrayList<Map>(sessionAssociations.toCollection()));
            }};
        }
    };

    private static F<SessionAssociation, Map> sessionAssociationToMap = new F<SessionAssociation, Map>() {
        public Map f(final SessionAssociation sessionAssociation) {

            if (sessionAssociation instanceof Attendance) {
                final Attendance attendance = (Attendance) sessionAssociation;
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("session", attendance.sessionId.value);
                map.put("attending", Boolean.TRUE);
                map.put("rating", toNull(attendance.rating.map(sessionRatingToString)));
                map.put("ratingComment", toNull(attendance.ratingComment));
                return map;
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("session", sessionAssociation.sessionId.value);
                map.put("attending", Boolean.FALSE);
                map.put("rating", null);
                map.put("ratingComment", null);
                return map;
            }
        }
    };

    private static F<Map, SessionAssociation> sessionAssociationFromMap = new F<Map, SessionAssociation>() {
        public SessionAssociation f(Map map) {
            SessionId sessionId = new SessionId(map.get("session").toString());

            if ((Boolean) map.get("attending")) {
                return Attendance.createAttendance(sessionId,
                    Option.<String>fromNull((String) map.get("rating")).map(sessionRatingFromString),
                    Option.<String>fromNull((String) map.get("ratingComment")));
            } else {
                return SessionInterrest.createInterest(sessionId);
            }
        }
    };

    public static F<SessionRating, String> sessionRatingToString = new F<SessionRating, String>() {
        public String f(SessionRating sessionRating) {
            return sessionRating.name();
        }
    };

    public static F<String, SessionRating> sessionRatingFromString = new F<String, SessionRating>() {
        public SessionRating f(String s) {
            return SessionRating.valueOf(s);
        }
    };

    public static <K, V> Effect<V> putEffect(final Map<K, V> map, final K key) {
        return new Effect<V>() {
            public void e(V v) {
                map.put(key, v);
            }
        };
    }
}
