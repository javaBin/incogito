package no.java.incogito.web.resources;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P1;
import fj.data.List;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionRating;
import no.java.incogito.domain.UserSessionAssociation;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.InterestLevelXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionRatingXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.dto.UserSessionAssociationXml;
import no.java.incogito.util.Enums;

import javax.ws.rs.core.UriBuilder;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlFunctions {
    public static final F<Event, EventXml> eventToXml = new F<Event, EventXml>() {
        public EventXml f(Event event) {
            return new EventXml(event.name);
        }
    };

    public static final F<List<Event>, List<EventXml>> eventListToXml = List.<Event, EventXml>map_().f(XmlFunctions.eventToXml);

    public static final F<P1<UriBuilder>, F<Session, SessionXml>> sessionToXml = curry(new F2<P1<UriBuilder>, Session, SessionXml>() {
        public SessionXml f(P1<UriBuilder> uriBuilder, Session session) {
            return new SessionXml(uriBuilder._1().segment(session.title).build().toString(), session.id.value, session.title);
        }
    });

    public static final F<UserSessionAssociation, UserSessionAssociationXml> sessionAssociationToXml = new F<UserSessionAssociation, UserSessionAssociationXml>() {
        public UserSessionAssociationXml f(UserSessionAssociation userSessionAssociation) {

            F<String, SessionRatingXml> f = Enums.<SessionRatingXml>valueOf().f(SessionRatingXml.class);

            return new UserSessionAssociationXml(userSessionAssociation.sessionId.value,
                    userSessionAssociation.rating.map(Enums.<SessionRating>toString_()).map(f).orSome(SessionRatingXml.NOT_SET),
                    userSessionAssociation.ratingComment,
                    InterestLevelXml.valueOf(userSessionAssociation.interestLevel.name()));
        }
    };

    public static final F<P1<UriBuilder>, F<Schedule, ScheduleXml>> scheduleToXml = curry(new F2<P1<UriBuilder>, Schedule, ScheduleXml>() {
        public ScheduleXml f(P1<UriBuilder> uriBuilder, Schedule schedule) {
            return new ScheduleXml(schedule.sessions.map(sessionToXml.f(uriBuilder)),
                    schedule.sessionAssociations.values().map(sessionAssociationToXml));
        }
    });
}
