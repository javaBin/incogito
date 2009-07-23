package no.java.incogito.application;

import fj.Effect;
import fj.F;
import static fj.P.p;
import fj.P4;
import fj.data.Java;
import fj.data.List;
import fj.data.Option;
import static fj.data.Option.fromNull;
import fj.data.Set;
import no.java.incogito.domain.AttendanceMarker;
import static no.java.incogito.domain.AttendanceMarker.createAttendance;
import static no.java.incogito.domain.AttendanceMarker.createInterest;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.SessionRating;
import no.java.incogito.domain.User;
import static no.java.incogito.domain.User.createPersistentUser;
import no.java.incogito.voldemort.VoldemortF;
import voldemort.client.StoreClient;
import voldemort.client.UpdateAction;
import voldemort.versioning.Versioned;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class UserClient {

    public static final String SCHEMA = "{" +
        "\"id\":\"string\", " +
        "\"attendanceMarkers\":[{" +
        "   \"session\":\"string\", " +
        "   \"attending\":\"boolean\", " +
        "   \"rating\":\"string\", " +
        "   \"ratingComment\":\"string\"}]" +
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

    public Effect<User> setUser = new Effect<User>() {
        public void e(User user) {
            setUser(user);
        }
    };

    // -----------------------------------------------------------------------
    // Utility Functions
    // -----------------------------------------------------------------------

    private static final F<Map, User> fromMap = new F<Map, User>() {
        public User f(Map map) {
            Set<AttendanceMarker> attendanceMarkers = Set.empty(AttendanceMarker.ord);

            System.out.println("fromMap: map = " + map);

            //noinspection unchecked
            for (Map markers : (java.util.List<Map>) map.get("attendanceMarkers")) {
                attendanceMarkers = attendanceMarkers.insert(attendanceMarkersFromMap.f(markers));
            }

            return createPersistentUser(no.java.incogito.domain.User.UserId.fromString.f(map.get("id").toString()), attendanceMarkers);
        }
    };

    private static final F<User, Map> toMap = new F<User, Map>() {
        public Map f(final User user) {
            final List<Map> attendanceMarkers = user.attendanceMarkers.toList().map(attendanceMarkersToMap);


            HashMap<String, Object> m = new HashMap<String, Object>() {{
                put("id", user.id.value);
                put("attendanceMarkers", Java.<Map>List_ArrayList().f(attendanceMarkers));
            }};
            System.out.println("toMap: m = " + m);
            return m;
        }
    };

    private static F<AttendanceMarker, Map> attendanceMarkersToMap = new F<AttendanceMarker, Map>() {
        public Map f(final AttendanceMarker attendanceMarker) {
            P4<SessionId, Boolean, Option<SessionRating>, Option<String>> p;

            if (attendanceMarker instanceof no.java.incogito.domain.AttendanceMarker.AttendingMarker) {
                no.java.incogito.domain.AttendanceMarker.AttendingMarker attendingMarker = (no.java.incogito.domain.AttendanceMarker.AttendingMarker) attendanceMarker;
                p = p(attendingMarker.sessionId,
                        Boolean.TRUE,
                        attendingMarker.rating,
                        attendingMarker.ratingComment
                );
            } else {
                p = p(attendanceMarker.sessionId,
                        Boolean.TRUE,
                        Option.<SessionRating>none(),
                        Option.<String>none());
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("session", p._1().value);
            map.put("attending", p._2());
            map.put("rating", p._3().map(sessionRatingToString).orSome((String)null));
            map.put("ratingComment", p._4().orSome((String)null));
            return map;
        }
    };

    private static F<Map, AttendanceMarker> attendanceMarkersFromMap = new F<Map, AttendanceMarker>() {
        public AttendanceMarker f(Map map) {
            SessionId sessionId = new SessionId(map.get("session").toString());

            if ((Boolean) map.get("attending")) {
                return createAttendance(sessionId,
                    Option.<String>fromNull((String) map.get("rating")).map(sessionRatingFromString),
                    Option.<String>fromNull((String) map.get("ratingComment")));
            } else {
                return createInterest(sessionId);
            }
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
