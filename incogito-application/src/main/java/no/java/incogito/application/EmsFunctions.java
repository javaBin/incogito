package no.java.incogito.application;

import fj.F;
import fj.F2;
import static fj.Function.compose;
import static fj.Function.curry;
import static fj.Function.flip;
import fj.P2;
import fj.data.Either;
import static fj.data.Either.left;
import static fj.data.Either.right;
import fj.data.List;
import static fj.data.List.iterableList;
import fj.data.Option;
import static fj.data.Option.fromNull;
import static fj.data.Option.fromString;
import static fj.data.Option.somes;
import fj.pre.Show;
import no.java.incogito.Enums;
import no.java.incogito.Functions;
import no.java.incogito.application.IncogitoConfiguration.DayConfiguration;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.WikiString;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

/**
 * Functions from EMS domain objects to Incogito domain objects.
 *
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsFunctions {
    public static F<no.java.ems.domain.Event, EventId> eventIdFromEms = new F<no.java.ems.domain.Event, EventId>() {
        public EventId f(no.java.ems.domain.Event event) {
            return EventId.eventId(event.getId());
        }
    };

    public static F<IncogitoConfiguration, F<no.java.ems.domain.Event, Either<String, Event>>> eventFromEms = curry(new F2<IncogitoConfiguration, no.java.ems.domain.Event, Either<String, Event>>() {
        public Either<String, Event> f(IncogitoConfiguration configuration, final no.java.ems.domain.Event event) {
            final EventId eventId = eventIdFromEms.f(event);

            return configuration.eventConfigurations.
                    find(compose(Functions.equals.f(event.getName()), EventConfiguration.name_)).
                    toEither("Could not find configured event '" + eventId + "'.").right().
                    map(new F<EventConfiguration, Event>() {
                        public Event f(EventConfiguration eventConfiguration) {
                            F<P2<LocalDate, DayConfiguration>, List<Room>> roomMap = new F<P2<LocalDate, DayConfiguration>, List<Room>>() {
                                public List<Room> f(P2<LocalDate, DayConfiguration> p2) {
                                    return p2._2().rooms;
                                }
                            };

                            F<P2<LocalDate, DayConfiguration>, List<Interval>> timeslotMap = new F<P2<LocalDate, DayConfiguration>, List<Interval>>() {
                                public List<Interval> f(P2<LocalDate, DayConfiguration> p2) {
                                    return p2._2().timeslots;
                                }
                            };

                            return new Event(eventId,
                                    event.getName(),
                                    eventConfiguration.blurb,
                                    eventConfiguration.frontPageText,
                                    eventConfiguration.presentationRooms,
                                    eventConfiguration.dayConfigurations.map(P2.<LocalDate, DayConfiguration>__1()),
                                    eventConfiguration.dayConfigurations.map(roomMap),
                                    eventConfiguration.dayConfigurations.map(timeslotMap),
                                    eventConfiguration.levels,
                                    eventConfiguration.labels,
                                    eventConfiguration.labelMap);
                        }
                    });
        }
    });

    public static F<no.java.ems.domain.Speaker, Speaker> speakerFromEms = new F<no.java.ems.domain.Speaker, Speaker>() {
        public Speaker f(no.java.ems.domain.Speaker speaker) {
            return new Speaker(speaker.getName(), speaker.getPersonId(), fromString(speaker.getDescription()).map(WikiString.constructor));
        }
    };

    public static F<Event, F<no.java.ems.domain.Session, Either<String, Session>>> sessionFromEms = curry(new F2<Event, no.java.ems.domain.Session, Either<String, Session>>() {
        public Either<String, Session> f(Event event, no.java.ems.domain.Session session) {
            if (session.getTitle() == null) {
                return left("Not a valid session, title is missing.");
            }

            // Hack for now until ';' is encoded in url properly
            if (session.getTitle().indexOf(';') > 0) {
                return left("Not a valid session, title has invalid character ';'.");
            }

            Option<LevelId> levelId = fromNull(session.getLevel()).
                bind(Functions.compose(LevelId.valueOf, Enums.<no.java.ems.domain.Session.Level>name_()));

            F<LevelId, Option<Level>> getLevel = flip(Functions.<LevelId, Level>TreeMap_get()).f(event.levels);
            F<String, Option<Label>> getLabel = flip(Functions.<String, Label>TreeMap_get()).f(event.emsIndexedLabels);
            
            F<Interval, Interval> setEndTo60MinutesAfterStart = new F<Interval, Interval>() {
            	public Interval f(Interval interval) {
            		return interval.withEnd(interval.getStart().plusHours(1));
            	}
        	};
            
            F<Interval, Interval> setEndTo15MinutesAfterStart = new F<Interval, Interval>() {
            	public Interval f(Interval interval) {
            		return interval.withEnd(interval.getStart().plusMinutes(15));
            	}
        	};

        	Session.Format format = fromNull(session.getFormat()).bind(compose(Session.Format.valueOf_, Show.<no.java.ems.domain.Session.Format>anyShow().showS_())).orSome(Session.Format.Presentation);

        	return right(new Session(new SessionId(session.getId()),
                format,
                session.getTitle(),
                fromString(session.getBody()).map(WikiString.constructor),
                levelId.bind(getLevel),
                fromNull(session.getTimeslot()).map(format == Session.Format.Presentation ? setEndTo60MinutesAfterStart : setEndTo15MinutesAfterStart),
                fromNull(session.getRoom()).map(no.java.incogito.ems.client.EmsFunctions.roomName),
                somes(iterableList(session.getKeywords()).map(getLabel)),
                iterableList(session.getSpeakers()).map(speakerFromEms),
                List.<Comment>nil()));
        }
    });
}
