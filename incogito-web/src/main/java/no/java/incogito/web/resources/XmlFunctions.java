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
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.WikiString;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.InterestLevelXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionRatingXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.dto.UserSessionAssociationXml;
import no.java.incogito.dto.SpeakerXml;
import no.java.incogito.util.Enums;

import javax.ws.rs.core.UriBuilder;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;

import org.joda.time.Interval;
import org.joda.time.DateTime;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class XmlFunctions {
    private static final DatatypeFactory datatypeFactory;

    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private static F<Interval, XMLGregorianCalendar> toXmlGregorianCalendar = new F<Interval, XMLGregorianCalendar>() {
        public XMLGregorianCalendar f(Interval interval) {
            XMLGregorianCalendar gregorianCalendar = datatypeFactory.newXMLGregorianCalendar();
            DateTime start = interval.getStart();
            gregorianCalendar.setTime(start.getHourOfDay(),start.getMinuteOfHour(), start.getSecondOfMinute());
            gregorianCalendar.setYear(start.getYear());
            gregorianCalendar.setMonth(start.getMonthOfYear());
            gregorianCalendar.setDay(start.getDayOfMonth());
            return gregorianCalendar;
        }
    };

    private static F<Speaker, SpeakerXml> speakerToXml = new F<Speaker, SpeakerXml>() {
        public SpeakerXml f(Speaker speaker) {
            return new SpeakerXml(speaker.name, speaker.bio.map(WikiString.toHtml));
        }
    };

    public static final F<P1<UriBuilder>, F<Session, SessionXml>> sessionToXml = curry(new F2<P1<UriBuilder>, Session, SessionXml>() {
        public SessionXml f(P1<UriBuilder> uriBuilder, Session session) {
            return new SessionXml(uriBuilder._1().segment(session.title).build().toString(),
                    session.id.value,
                    session.title,
                    session._abstract.map(WikiString.toHtml),
                    session.body.map(WikiString.toHtml),
                    session.room,
                    session.timeslot.map(toXmlGregorianCalendar),
                    session.speakers.map(speakerToXml));
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


    public static final F<Event, EventXml> eventToXml = new F<Event, EventXml>() {
        public EventXml f(Event event) {
            return new EventXml(event.name);
        }
    };

    public static final F<List<Event>, List<EventXml>> eventListToXml = List.<Event, EventXml>map_().f(XmlFunctions.eventToXml);
}
