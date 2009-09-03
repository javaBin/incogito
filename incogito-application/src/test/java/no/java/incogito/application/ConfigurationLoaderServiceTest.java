package no.java.incogito.application;

import fj.P;
import fj.data.List;
import static fj.data.List.list;
import fj.data.TreeMap;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.ems.client.RestEmsService;
import no.java.ems.domain.Event;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Room;
import no.java.incogito.ems.client.EmsWrapper;
import org.joda.time.LocalDate;
import org.joda.time.Interval;
import org.joda.time.DateTime;

import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ConfigurationLoaderServiceTest extends TestCase {
    private LocalDate sep17th = new LocalDate(2008, 9, 17);
    private LocalDate sep18th = new LocalDate(2008, 9, 18);
    private List<Room> roomsDay1 = list(new Room("Lab I"), new Room("Lab II"), new Room("BoF"));
    private List<Room> roomsDay2 = list(new Room("Lab I"), new Room("Lab II"));

    public void testReconfigure() throws Exception {
        EmsWrapper emsWrapper = new EmsWrapper(new RestEmsService(null));
        Event event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setName("JavaZone 2009");

        final TreeMap<String, Event> events = TreeMap.<String, Event>empty(Ord.stringOrd).set(event.getName(), event);
        emsWrapper.getEvents = P.p(events.values());
        emsWrapper.findEventByName = DefaultIncogitoApplicationTest.mockFindEventByName.f(events);
        DefaultIncogitoApplication application = new DefaultIncogitoApplication(DefaultIncogitoApplicationTest.getIncogitoHome(), null, emsWrapper);

        application.afterPropertiesSet();

        IncogitoConfiguration configuration = application.getConfiguration();

        EventConfiguration eventConfiguration = configuration.findEventConfigurationByName(event.getName()).some();

        assertEquals(2, eventConfiguration.dayConfigurations.length());
        assertEquals(sep17th, eventConfiguration.dayConfigurations.index(0)._1());
        assertEquals(sep18th, eventConfiguration.dayConfigurations.index(1)._1());

        assertEquals(2, eventConfiguration.dayConfigurations.index(0)._2().rooms.length());
        assertEquals(new Room("Lab I"), eventConfiguration.dayConfigurations.index(0)._2().rooms.index(0));
        assertEquals(new Room("Lab II"), eventConfiguration.dayConfigurations.index(0)._2().rooms.index(1));

        DateTime s17 = sep17th.toDateMidnight().toDateTime();
        DateTime s18 = sep17th.toDateMidnight().toDateTime();

        assertEquals(8, eventConfiguration.dayConfigurations.index(0)._2().timeslots.length());
        assertEquals(new Interval(s17.withHourOfDay(9), s17.withHourOfDay(10)), eventConfiguration.dayConfigurations.index(0)._2().timeslots.index(0));
        assertEquals(new Interval(s17.withHourOfDay(18).withMinuteOfHour(15), s17.withHourOfDay(19).withMinuteOfHour(15)), eventConfiguration.dayConfigurations.index(0)._2().timeslots.index(7));

        assertEquals(3, eventConfiguration.dayConfigurations.index(1)._2().rooms.length());
        assertEquals(new Room("Lab I"), eventConfiguration.dayConfigurations.index(1)._2().rooms.index(0));
        assertEquals(new Room("Lab II"), eventConfiguration.dayConfigurations.index(1)._2().rooms.index(1));
        assertEquals(new Room("BoF"), eventConfiguration.dayConfigurations.index(1)._2().rooms.index(2));

        assertEquals(2, eventConfiguration.labels.length());
        assertEquals(eventConfiguration.labels.length(), eventConfiguration.labelMap.size());
        Label actualMyLabel = eventConfiguration.labelMap.get("MyLabel").some();
        assertEquals("My label", actualMyLabel.displayName);
        assertEquals("MyLabel", actualMyLabel.id);
        assertEquals("MyLabel", actualMyLabel.emsId);

        Label actualRenamedLabel = eventConfiguration.labelMap.get("renamed-label").some();
        assertEquals("Renamed Label", actualRenamedLabel.displayName);
        assertEquals("renamed-label", actualRenamedLabel.id);
        assertEquals("Renamed label", actualRenamedLabel.emsId);

        assertEquals("MyLabel", eventConfiguration.labels.index(0).id);
        assertEquals("renamed-label", eventConfiguration.labels.index(1).id);

        assertEquals(5, eventConfiguration.levels.size());
    }
}
