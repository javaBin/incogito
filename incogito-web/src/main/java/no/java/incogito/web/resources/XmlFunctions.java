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
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Label;
import no.java.incogito.dto.EventXml;
import no.java.incogito.dto.InterestLevelXml;
import no.java.incogito.dto.ScheduleXml;
import no.java.incogito.dto.SessionRatingXml;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.dto.UserSessionAssociationXml;
import no.java.incogito.dto.SpeakerXml;
import no.java.incogito.dto.LabelXml;
import no.java.incogito.Enums;
import static no.java.incogito.Functions.compose;

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

    public static F<Interval, DateTime> Interval_start = new F<Interval, DateTime>() {
        public DateTime f(Interval interval) {
            return interval.getStart();
        }
    };

    public static F<Interval, DateTime> Interval_end = new F<Interval, DateTime>() {
        public DateTime f(Interval interval) {
            return interval.getEnd();
        }
    };

    private static F<DateTime, XMLGregorianCalendar> toXmlGregorianCalendar = new F<DateTime, XMLGregorianCalendar>() {
        public XMLGregorianCalendar f(DateTime dateTime) {
            XMLGregorianCalendar gregorianCalendar = datatypeFactory.newXMLGregorianCalendar();
            gregorianCalendar.setTime(dateTime.getHourOfDay(),dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
            gregorianCalendar.setYear(dateTime.getYear());
            gregorianCalendar.setMonth(dateTime.getMonthOfYear());
            gregorianCalendar.setDay(dateTime.getDayOfMonth());
            return gregorianCalendar;
        }
    };

    private static F<Speaker, SpeakerXml> speakerToXml = new F<Speaker, SpeakerXml>() {
        public SpeakerXml f(Speaker speaker) {
            return new SpeakerXml(speaker.name, speaker.bio.map(WikiString.toHtml));
        }
    };

    private static F<Label, LabelXml> labelToXml = new F<Label, LabelXml>() {
        public LabelXml f(Label label) {
            return new LabelXml(label.id, label.displayName);
        }
    };

    public static final F<P1<UriBuilder>, F<Session, SessionXml>> sessionToXml = curry(new F2<P1<UriBuilder>, Session, SessionXml>() {
        public SessionXml f(P1<UriBuilder> uriBuilder, Session session) {
            return new SessionXml(uriBuilder._1().segment(session.title).build().toString(),
                    session.id.value,
                    session.title,
                    session._abstract.map(WikiString.toHtml),
                    session.body.map(WikiString.toHtml),
                    session.level.map(Level.showId.showS_()),
                    session.room,
                    session.timeslot.map(compose(toXmlGregorianCalendar, Interval_start)),
                    session.timeslot.map(compose(toXmlGregorianCalendar, Interval_end)),
                    session.speakers.map(speakerToXml),
                    session.labels.map(labelToXml));
        }
    });

    public static final F<UserSessionAssociation, UserSessionAssociationXml> sessionAssociationToXml = new F<UserSessionAssociation, UserSessionAssociationXml>() {
        public UserSessionAssociationXml f(UserSessionAssociation userSessionAssociation) {

            return new UserSessionAssociationXml(userSessionAssociation.sessionId.value,
                    userSessionAssociation.rating.map(Enums.<SessionRating>toString_()).bind(SessionRatingXml.valueOf_).orSome(SessionRatingXml.NOT_SET),
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
            return new EventXml(event.id.toString(), event.name, event.welcome);
        }
    };

    public static final F<List<Event>, List<EventXml>> eventListToXml = List.<Event, EventXml>map_().f(XmlFunctions.eventToXml);
}
