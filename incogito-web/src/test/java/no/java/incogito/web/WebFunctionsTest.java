package no.java.incogito.web;

import fj.F;
import static fj.P.p;
import fj.P2;
import fj.data.List;
import static fj.data.List.list;
import static fj.data.List.single;
import fj.data.Option;
import static fj.data.Option.some;
import fj.data.TreeMap;
import junit.framework.TestCase;
import no.java.incogito.Functions;
import no.java.incogito.application.IncogitoConfiguration;
import no.java.incogito.application.IncogitoConfiguration.DayConfiguration;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;
import static no.java.incogito.application.IncogitoConfiguration.unconfigured;
import no.java.incogito.domain.Comment;
import no.java.incogito.domain.CssConfiguration;
import no.java.incogito.domain.IncogitoTestData;
import static no.java.incogito.domain.IncogitoTestData.javaZone2008;
import static no.java.incogito.domain.IncogitoTestData.jz08Day1Rooms;
import static no.java.incogito.domain.IncogitoTestData.jz08Day1Timeslots;
import static no.java.incogito.domain.IncogitoTestData.jz08Day2Rooms;
import static no.java.incogito.domain.IncogitoTestData.jz08Day2Timeslots;
import static no.java.incogito.domain.IncogitoTestData.sep17th;
import static no.java.incogito.domain.IncogitoTestData.sep18th;
import no.java.incogito.domain.IncogitoUri;
import no.java.incogito.domain.IncogitoUri.IncogitoEventsUri.IncogitoEventUri;
import no.java.incogito.domain.IncogitoUri.IncogitoRestEventsUri.IncogitoRestEventUri;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import static no.java.incogito.domain.Session.Format.Presentation;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.Speaker;
import no.java.incogito.domain.UserSessionAssociation;
import no.java.incogito.domain.WikiString;
import no.java.incogito.web.servlet.WebCalendar;
import org.joda.time.DateMidnight;
import org.joda.time.Duration;
import org.joda.time.Interval;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class WebFunctionsTest extends TestCase {

    DayConfiguration jz08Day1 = new DayConfiguration(jz08Day1Rooms, jz08Day1Timeslots);
    DayConfiguration jz08Day2 = new DayConfiguration(jz08Day2Rooms, jz08Day2Timeslots);

    EventConfiguration jz08Configuration = new EventConfiguration(javaZone2008.name,
            Option.<String>none(),
            list(p(sep17th, jz08Day1), p(sep18th, jz08Day2)),
            List.<Room>nil(),
            List.<Label>nil(),
            TreeMap.<LevelId, Level>empty(LevelId.ord),
            0);

    Session templateSession = new Session(new SessionId("123"),
            Presentation,
            "Session 1",
            Option.<WikiString>none(),
            Option.<Level>none(),
            Option.<Interval>none(),
            Option.<String>none(),
            List.<Label>nil(),
            List.<Speaker>nil(),
            List.<Comment>nil());

    Option<String> room1 = some("Room 1");
    Option<String> room2 = some("Room 2");

    Option<Interval> day1 = some(new Interval(new DateMidnight(2009, 9, 9), new Duration(1000)));
    Option<Interval> day2 = some(new Interval(new DateMidnight(2009, 9, 10), new Duration(1000)));

    CssConfiguration cssConfiguration = new CssConfiguration(2.5, 20.0 / 60.0, 11);

    IncogitoConfiguration configuration = new IncogitoConfiguration(unconfigured.baseurl, cssConfiguration, Option.<String>none(), Option.<String>none(),
        single(jz08Configuration));

    public void testFunctions() {
        List<Room> rooms = List.list(new Room("Room 1"), new Room("Room 2"));
        String s = WebFunctions.generateCalendarCss.f(cssConfiguration).f(rooms).foldRight(Functions.String_join.f("\n"), "");

        assertEquals(".start0900 { top: 2.5em; }\n" +
                ".start0915 { top: 5.8em; }\n" +
                ".start0930 { top: 9.2em; }\n" +
                ".start0945 { top: 12.5em; }\n" +
                ".start1000 { top: 15.8em; }\n" +
                ".start1015 { top: 19.2em; }\n" +
                ".start1030 { top: 22.5em; }\n" +
                ".start1045 { top: 25.8em; }\n" +
                ".start1100 { top: 29.2em; }\n" +
                ".start1115 { top: 32.5em; }\n" +
                ".start1130 { top: 35.8em; }\n" +
                ".start1145 { top: 39.2em; }\n" +
                ".start1200 { top: 42.5em; }\n" +
                ".start1215 { top: 45.8em; }\n" +
                ".start1230 { top: 49.2em; }\n" +
                ".start1245 { top: 52.5em; }\n" +
                ".start1300 { top: 55.8em; }\n" +
                ".start1315 { top: 59.2em; }\n" +
                ".start1330 { top: 62.5em; }\n" +
                ".start1345 { top: 65.8em; }\n" +
                ".start1400 { top: 69.2em; }\n" +
                ".start1415 { top: 72.5em; }\n" +
                ".start1430 { top: 75.8em; }\n" +
                ".start1445 { top: 79.2em; }\n" +
                ".start1500 { top: 82.5em; }\n" +
                ".start1515 { top: 85.8em; }\n" +
                ".start1530 { top: 89.2em; }\n" +
                ".start1545 { top: 92.5em; }\n" +
                ".start1600 { top: 95.8em; }\n" +
                ".start1615 { top: 99.2em; }\n" +
                ".start1630 { top: 102.5em; }\n" +
                ".start1645 { top: 105.8em; }\n" +
                ".start1700 { top: 109.2em; }\n" +
                ".start1715 { top: 112.5em; }\n" +
                ".start1730 { top: 115.8em; }\n" +
                ".start1745 { top: 119.2em; }\n" +
                ".start1800 { top: 122.5em; }\n" +
                ".start1815 { top: 125.8em; }\n" +
                ".start1830 { top: 129.2em; }\n" +
                ".start1845 { top: 132.5em; }\n" +
                ".start1900 { top: 135.8em; }\n" +
                ".start1915 { top: 139.2em; }\n" +
                ".start1930 { top: 142.5em; }\n" +
                ".start1945 { top: 145.8em; }\n" +
                ".duration10 { height: 10em; margin: 0; padding: 0; }\n" +
                ".duration60 { height: 10em; margin: 0; padding: 0; }\n", s);
    }

/*
    public void testFunctions() {
        List<Room> rooms = List.list(new Room("Room 1"), new Room("Room 2"));
        F<List<Room>, List<String>> generateCalendarCss = WebFunctions.generateCalendarCss.
                f(cssConfiguration).
                f(configuration.findEventConfigurationByName(javaZone2008.name).some());
        String s = generateCalendarCss.f(rooms).foldRight(Functions.String_join.f("\n"), "");

        assertEquals(".start0900 { top: 2.5em; }\n" +
                ".start1015 { top: 5.8em; }\n" +
                ".start1145 { top: 9.2em; }\n" +
                ".start1300 { top: 12.5em; }\n" +
                ".start1415 { top: 15.8em; }\n" +
                ".start1545 { top: 19.2em; }\n" +
                ".start1700 { top: 22.5em; }\n" +
                ".start1815 { top: 25.8em; }\n" +
                ".start0900 { top: 29.2em; }\n" +
                ".start1015 { top: 32.5em; }\n" +
                ".start1145 { top: 35.8em; }\n" +
                ".start1300 { top: 39.2em; }\n" +
                ".start1415 { top: 42.5em; }\n" +
                ".start1545 { top: 45.8em; }\n" +
                ".start1700 { top: 49.2em; }\n" +
                ".start1815 { top: 52.5em; }\n" +
                ".duration10 { height: 10em; margin: 0; padding: 0; }\n" +
                ".duration15 { height: 10em; margin: 0; padding: 0; }\n" +
                ".duration60 { height: 10em; margin: 0; padding: 0; }\n", s);
    }
*/
    
    public void testSessionCss() {
        List<String> lines = WebFunctions.generateSessionCss.f(configuration).f(javaZone2008);
        String s = lines.foldRight(Functions.String_join.f("\n"), "");
        System.out.println(s);
        assertEquals(".level-Intermediate { list-style-image: url('http://unconfigured/rest/events/JavaZone 2008/icons/levels/Intermediate.png'); }", lines.index(3));
    }

    public void testDuration() {
        F<Integer, String> f = WebFunctions.durationToCss.f(cssConfiguration);

        System.out.println("cssConfiguration.emPerMinute = " + cssConfiguration.emPerMinute);
        assertEquals(".duration15 { height: 10em; margin: 0; padding: 0; }", f.f(15));

//        assertEquals(".duration15 { height: 5.0em; margin: 0; padding: 0; }", f.f(15));
//        assertEquals(".duration30 { height: 10.0em; margin: 0; padding: 0; }", f.f(30));
//        assertEquals(".duration60 { height: 20.0em; margin: 0; padding: 0; }", f.f(60));
    }

    public void testHourToCss() {
        F<P2<P2<String, String>, Integer>, String> f = WebFunctions.hourToSessionCss.f(cssConfiguration);

        assertEquals(".start0900 { top: 2.5em; }", f.f(p(p("09", "00"), 0)));
        assertEquals(".start0915 { top: 5.8em; }", f.f(p(p("09", "15"), 1)));
        assertEquals(".start0930 { top: 9.2em; }", f.f(p(p("09", "30"), 2)));

//        F<P2<String, Integer>, String> f = WebFunctions.hourToSessionCss.f(cssConfiguration);
//
//        assertEquals(".start0900 { top: 2.5em; }", f.f(p("0900", 0)));
//        assertEquals(".start0915 { top: 5.8em; }", f.f(p("0915", 1)));
//        assertEquals(".start0930 { top: 9.2em; }", f.f(p("0930", 2)));
    }

    public void testEmptyCalendar() {
        WebCalendar calendar = createCalendar(list(templateSession));
        assertEquals(2, calendar.getDayToRoomToPresentationsMap().size());
        assertEquals(2, calendar.getRoomsByDate().size());
        assertEquals(3, calendar.getRoomsByDate().get(sep17th).size());
        assertEquals(2, calendar.getRoomsByDate().get(sep18th).size());

        calendar = createCalendar(list(templateSession.room(room1), templateSession.room(room2)));
        assertEquals(2, calendar.getDayToRoomToPresentationsMap().size());
        assertEquals(2, calendar.getRoomsByDate().size());
        assertEquals(3, calendar.getRoomsByDate().get(sep17th).size());
        assertEquals(2, calendar.getRoomsByDate().get(sep18th).size());

        calendar = createCalendar(list(templateSession.timeslot(day1), templateSession.timeslot(day2)));
        assertEquals(2, calendar.getRoomsByDate().size());
        assertEquals(3, calendar.getRoomsByDate().get(sep17th).size());
        assertEquals(2, calendar.getRoomsByDate().get(sep18th).size());
    }

    public void testCalendarWithSessions() {
        WebCalendar calendar = createCalendar(IncogitoTestData.sessions);
        assertEquals(2, calendar.getRoomsByDate().size());
        assertEquals(3, calendar.getRoomsByDate().get(sep17th).size());
        assertEquals(2, calendar.getRoomsByDate().get(sep18th).size());
        assertEquals(2, calendar.getDayToRoomToPresentationsMap().size());
        assertEquals(2, calendar.getQuickiesByDay().size());
    }

    private WebCalendar createCalendar(List<Session> sessions) {
        IncogitoUri incogitoUri = new IncogitoUri("poop");
        IncogitoRestEventUri restEventUri = incogitoUri.restEvents().eventUri("myevent");
        IncogitoEventUri eventUri = incogitoUri.events().eventUri("myevent");
        Schedule schedule = new Schedule(javaZone2008, sessions, TreeMap.<SessionId, UserSessionAssociation>empty(SessionId.ord));
        return WebFunctions.webCalendar.f(restEventUri).f(eventUri).f(schedule);
    }
}
