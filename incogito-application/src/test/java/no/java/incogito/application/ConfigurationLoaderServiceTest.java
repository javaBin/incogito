package no.java.incogito.application;

import fj.P;
import fj.data.TreeMap;
import fj.data.List;
import static fj.data.List.list;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.ems.client.RestEmsService;
import no.java.ems.domain.Event;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Room;
import no.java.incogito.ems.client.EmsWrapper;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;

import java.util.UUID;

import org.joda.time.LocalDate;

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

        assertEquals(2, eventConfiguration.roomsByDate.length());
        assertEquals(sep17th, eventConfiguration.roomsByDate.index(0)._1());
        assertEquals(new Room("Lab I"), eventConfiguration.roomsByDate.index(0)._2().index(0));
        assertEquals(new Room("Lab II"), eventConfiguration.roomsByDate.index(0)._2().index(1));
        assertEquals(sep18th, eventConfiguration.roomsByDate.index(1)._1());
        assertEquals(new Room("Lab I"), eventConfiguration.roomsByDate.index(1)._2().index(0));
        assertEquals(new Room("Lab II"), eventConfiguration.roomsByDate.index(1)._2().index(1));
        assertEquals(new Room("BoF"), eventConfiguration.roomsByDate.index(1)._2().index(2));

        assertEquals(2, eventConfiguration.labels.size());
        Label actualMyLabel = eventConfiguration.labels.get("MyLabel").some();
        assertEquals("My label", actualMyLabel.displayName);
        assertEquals("MyLabel", actualMyLabel.id);
        assertEquals("MyLabel", actualMyLabel.emsId);

        Label actualRenamedLabel = eventConfiguration.labels.get("renamed-label").some();
        assertEquals("Renamed Label", actualRenamedLabel.displayName);
        assertEquals("renamed-label", actualRenamedLabel.id);
        assertEquals("Renamed label", actualRenamedLabel.emsId);

        assertEquals(5, eventConfiguration.levels.size());
    }
}
