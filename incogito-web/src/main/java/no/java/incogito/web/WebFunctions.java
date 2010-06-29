package no.java.incogito.web;

import static fj.Function.curry;
import static fj.data.List.list;
import static fj.data.Set.empty;
import static fj.data.Stream.stream;
import static fj.pre.Ord.stringOrd;
import static no.java.incogito.Functions.compose;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import no.java.incogito.Functions;
import no.java.incogito.application.IncogitoConfiguration;
import no.java.incogito.domain.CssConfiguration;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.UserSessionAssociation;
import no.java.incogito.domain.IncogitoUri.IncogitoEventsUri.IncogitoEventUri;
import no.java.incogito.domain.IncogitoUri.IncogitoRestEventsUri.IncogitoRestEventUri;
import no.java.incogito.domain.Session.Format;
import no.java.incogito.dto.SessionXml;
import no.java.incogito.web.resources.XmlFunctions;
import no.java.incogito.web.servlet.WebCalendar;
import no.java.incogito.web.servlet.WebSessionList;

import org.joda.time.Interval;
import org.joda.time.LocalDate;

import fj.F;
import fj.F2;
import fj.F3;
import fj.P;
import fj.P2;
import fj.data.List;
import fj.data.Set;
import fj.data.Stream;
import fj.data.TreeMap;
import fj.pre.Ord;
import fj.pre.Ordering;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebFunctions {

    private static final NumberFormat oneDigitFormat;

    static {
        oneDigitFormat = DecimalFormat.getNumberInstance(Locale.ENGLISH);
        oneDigitFormat.setMaximumFractionDigits(1);
        oneDigitFormat.setMinimumFractionDigits(1);
    }

    // -----------------------------------------------------------------------
    // Calendar CSS
    // -----------------------------------------------------------------------

    public static final F<CssConfiguration, F<List<Room>, List<String>>> generateCalendarCss = curry(new F2<CssConfiguration, List<Room>, List<String>>() {
        public List<String> f(CssConfiguration cssConfiguration, List<Room> roomList) {

            List<String> sessions = Functions.List_product(list("09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"),
                    list("00", "15", "30", "45"), P.<String, String>p2()).
                    zipIndex().
                    map(hourToSessionCss.f(cssConfiguration));

            Stream<String> durations = stream(10, 60).zapp(Stream.repeat(durationToCss.f(cssConfiguration)));

            return List.join(list(sessions, durations.toList()));
        }
    });

    public static final F<CssConfiguration, F<Integer, String>> durationToCss = curry(new F2<CssConfiguration, Integer, String>() {
        public String f(CssConfiguration cssConfiguration, Integer minutes) {
            return ".duration" + minutes + " { height: 10em; margin: 0; padding: 0; }";
        }
    });

    public static final F<CssConfiguration, F<P2<P2<String, String>, Integer>, String>> hourToSessionCss = curry(new F2<CssConfiguration, P2<P2<String, String>, Integer>, String>() {
        F<P2<String, String>, String> prepend = P2.tuple(Functions.prepend);

        public String f(CssConfiguration cssConfiguration, P2<P2<String, String>, Integer> p) {
            double em = cssConfiguration.sessionEmStart + (cssConfiguration.getHeightInEm(p._2() * 10));
            return ".start" + prepend.f(p._1()) + " { top: " + oneDigitFormat.format(em) + "em; }";
        }
    });


    // -----------------------------------------------------------------------
    // Session CSS
    // -----------------------------------------------------------------------

    public static F<IncogitoConfiguration, F<Event, List<String>>> generateSessionCss = curry( new F2<IncogitoConfiguration, Event, List<String>>() {
        public List<String> f(final IncogitoConfiguration configuration, final Event event) {
            List<String> formats = list(Session.Format.values()).
                map(Session.Format.toString).
                map(new F<String, String>() {
                    public String f(String format) {
                        return ".format-" + format + " { list-style-image: url('" + configuration.baseurl + "/images/icons/session-format-" + format.toLowerCase() + "-small.png'); }";
                    }
                });

            List<String> levels = event.levels.values().
                map(Level.showId.showS_()).
                map(new F<String, String>() {
                    public String f(String level) {
                        return ".level-" + level + " { list-style-image: url('" + configuration.baseurl + "/rest/events/" + event.name + "/icons/levels/" + level + ".png'); }";
                    }
                });

            List<String> labels = event.labels.
                map(new F<Label, String>() {
                    public String f(Label label) {
                        return ".label-" + label.id + " { list-style-image: url('" + configuration.baseurl + "/rest/events/" + event.name + "/icons/labels/" + label.id + ".png'); }";
                    }
                });

            return formats.append(levels).append(labels);
        }
    });

    // -----------------------------------------------------------------------
    // Calendar
    // -----------------------------------------------------------------------

    public static final F<IncogitoRestEventUri, F<IncogitoEventUri, F<Schedule, WebCalendar>>> webCalendar = curry(new F3<IncogitoRestEventUri, IncogitoEventUri, Schedule, WebCalendar>() {
        public WebCalendar f(IncogitoRestEventUri restEventUri, IncogitoEventUri eventUri, Schedule schedule) {
            F<Session,SessionXml> sessionToXml = XmlFunctions.sessionToXml.f(restEventUri).f(eventUri);

            Map<String, String> attendanceMap = new HashMap<String, String>();

            for (P2<SessionId, UserSessionAssociation> sessionAssociation : schedule.sessionAssociations) {
                attendanceMap.put(sessionAssociation._1().value, sessionAssociation._2().interestLevel.name());
            }

            LinkedHashMap<LocalDate, Collection<String>> roomsByDate = new LinkedHashMap<LocalDate, Collection<String>>();
            LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate = new LinkedHashMap<LocalDate, Collection<Interval>>();
            TreeMap<LocalDate, TreeMap<String, List<SessionXml>>> dayToRoomToPresentationsMap = TreeMap.empty(Functions.LocalDate_ord);
            TreeMap<LocalDate, List<SessionXml>> quickiesByDay = TreeMap.empty(Functions.LocalDate_ord);

            for (final P2<LocalDate, Integer> dayIndex : schedule.event.dates.zipIndex()) {
                final LocalDate day = dayIndex._1();
                final List<Room> rooms = schedule.event.roomsByDate.index(dayIndex._2());
                final List<Interval> timeslots = schedule.event.timeslotsByDate.index(dayIndex._2());

                roomsByDate.put(day, rooms.map(Room.name_).toCollection());
                timeslotsByDate.put(day, timeslots.toCollection());

                // -----------------------------------------------------------------------
                // Presentations
                // -----------------------------------------------------------------------

                F<Session, Boolean> presentationFilter = new F<Session, Boolean>() {
                    public Boolean f(Session session) {
                        return session.timeslot.isSome() &&
                                session.timeslot.some().getStart().toLocalDate().equals(day) &&
                                session.room.isSome() &&
                                rooms.find(compose(Functions.equals.f(session.room.some()), Room.name_)).isSome() &&
                                (session.format.equals(Format.Presentation) || session.format.equals(Format.BoF));
                    }
                };

                TreeMap<String, List<SessionXml>> roomToSessionMap = TreeMap.empty(stringOrd);

                // Create an empty list for each day, just to make sure that every day is covered. Other parts rely on this fact
                List<SessionXml> emptyList = List.nil();
                for (Room room : rooms) {
                    roomToSessionMap = roomToSessionMap.set(room.name, emptyList);
                }

                // For each session, find the room's list and add the session to the list
                for (SessionXml session : schedule.sessions.filter(presentationFilter).map(sessionToXml)) {
                    roomToSessionMap = roomToSessionMap.set(session.room, roomToSessionMap.get(session.room).some().cons(session));
                }

                dayToRoomToPresentationsMap = dayToRoomToPresentationsMap.set(dayIndex._1(), roomToSessionMap);

                // -----------------------------------------------------------------------
                // Lightning Talks
                // -----------------------------------------------------------------------

                // Do not check against the room list, just assume it is ok.
                F<Session, Boolean> lightningTalkFilter = new F<Session, Boolean>() {
                    public Boolean f(Session session) {
                        return session.timeslot.isSome() &&
                                session.timeslot.some().getStart().toLocalDate().equals(day) &&
                                session.room.isSome() &&
                                session.format.equals(Format.Quickie);
                    }
                };

                Set<Session> quickies = empty(reverseSessionTimestampOrd);

                for (Session session : schedule.sessions.filter(lightningTalkFilter)) {
                    quickies = quickies.insert(session);
                }

                quickiesByDay = quickiesByDay.set(day, quickies.toList().map(sessionToXml));
            }

            return new WebCalendar(attendanceMap, roomsByDate, timeslotsByDate,
                    dayToRoomToPresentationsMap, quickiesByDay);
        }
    });
    
    
    // WebSessionList
    public static final F<IncogitoRestEventUri, F<IncogitoEventUri, F<Schedule, WebSessionList>>> webSessionList = curry(new F3<IncogitoRestEventUri, IncogitoEventUri, Schedule, WebSessionList>() {
        public WebSessionList f(IncogitoRestEventUri restEventUri, IncogitoEventUri eventUri, Schedule schedule) {
            F<Session,SessionXml> sessionToXml = XmlFunctions.sessionToXml.f(restEventUri).f(eventUri);


            LinkedHashMap<LocalDate, Collection<Interval>> timeslotsByDate = new LinkedHashMap<LocalDate, Collection<Interval>>();
            LinkedHashMap<LocalDate, LinkedHashMap <Interval, List<SessionXml>>> sessionsByTimeSlotByDate = new LinkedHashMap<LocalDate, LinkedHashMap<Interval,List<SessionXml>>>();

            for (final P2<LocalDate, Integer> dayIndex : schedule.event.dates.zipIndex()) {
                final LocalDate day = dayIndex._1();
                final List<Room> rooms = schedule.event.roomsByDate.index(dayIndex._2());
                final List<Interval> timeslots = schedule.event.timeslotsByDate.index(dayIndex._2());

                timeslotsByDate.put(day, timeslots.toCollection());


                F<Session, Boolean> presentationFilter = new F<Session, Boolean>() {
                    public Boolean f(Session session) {
                        return session.timeslot.isSome() &&
                                session.timeslot.some().getStart().toLocalDate().equals(day) &&
                                session.room.isSome() &&
                                rooms.find(compose(Functions.equals.f(session.room.some()), Room.name_)).isSome() &&
                                (session.format.equals(Format.Presentation) || session.format.equals(Format.BoF));
                    }
                };
                
                F<Session, Boolean> lightningTalkFilter = new F<Session, Boolean>() {
                	public Boolean f(Session session) {
                		return session.timeslot.isSome() &&
                		session.timeslot.some().getStart().toLocalDate().equals(day) &&
                		session.room.isSome() &&
                		session.format.equals(Format.Quickie);
                	}
                };
                
                // Create an empty list for each day, just to make sure that every day is covered. Other parts rely on this fact
                
                LinkedHashMap<Interval, List<SessionXml>> sessionByTimeSlotsMap =
                	new LinkedHashMap<Interval, List<SessionXml>>();

                List<Session> quickies = schedule.sessions.filter(lightningTalkFilter);
                
                
                for(final Interval timeslot:timeslots) {                	
                    F<Session, Boolean> quickieToTimeslotFilter = new F<Session, Boolean>() {
                    	public Boolean f(Session session) {
                    		return session.timeslot.some().overlaps(timeslot);
                    	}
                    };
                	
                	// putter lyntaler inn per sesjon
                	sessionByTimeSlotsMap.put(timeslot, quickies.filter(quickieToTimeslotFilter).sort(sessionTimestampOrd).map(sessionToXml));
                }

                for (Session session : schedule.sessions.filter(presentationFilter).sort(sessionRoomOrd)) {
                	List<SessionXml> list = sessionByTimeSlotsMap.get(session.timeslot.some());
                	if (list != null){
                		sessionByTimeSlotsMap.put(session.timeslot.some(), list.cons(sessionToXml.f(session)));
                	}
                }

                sessionsByTimeSlotByDate.put(day, sessionByTimeSlotsMap);
            }
            return new WebSessionList(timeslotsByDate, sessionsByTimeSlotByDate);
        }
    });

    public static final Ord<Session> reverseSessionTimestampOrd = Ord.ord(curry(new F2<Session, Session, Ordering>() {
        public Ordering f(Session a, Session b) {
            return Ord.longOrd.compare(b.timeslot.some().getStartMillis(), a.timeslot.some().getStartMillis());
        }
    }));

    public static final Ord<Session> sessionTimestampOrd = Ord.ord(curry(new F2<Session, Session, Ordering>() {
        public Ordering f(Session a, Session b) {
            return Ord.longOrd.compare(a.timeslot.some().getStartMillis(), b.timeslot.some().getStartMillis());
        }
    }));
    
    public static final Ord<Session> sessionRoomOrd = Ord.ord(curry(new F2<Session, Session, Ordering>() {
        public Ordering f(Session a, Session b) {
            return Ord.stringOrd.compare(b.room.some(), a.room.some());
        }
    }));
}
