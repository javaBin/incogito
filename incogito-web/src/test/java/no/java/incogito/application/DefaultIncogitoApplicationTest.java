package no.java.incogito.application;

import static fj.Function.flip;
import fj.P;
import fj.data.TreeMap;
import fj.data.List;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.ems.client.RestEmsService;
import no.java.ems.domain.Event;
import no.java.incogito.Functions;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.domain.Label;
import no.java.incogito.ems.client.EmsWrapper;
import no.java.incogito.util.TestPathFactoryBean;

import java.io.File;
import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultIncogitoApplicationTest extends TestCase {

    File incogitoHome;

    protected void setUp() throws Exception {
        TestPathFactoryBean testPathFactoryBean = new TestPathFactoryBean();
        testPathFactoryBean.setTestClass(getClass());
        testPathFactoryBean.setPath("src/test/resources/configuration-loading");
        incogitoHome = (File) testPathFactoryBean.getObject();
    }

    public void testReconfigure() throws Exception {
        EmsWrapper emsWrapper = new EmsWrapper(new RestEmsService(null));
        Event event = new Event();
        event.setId(UUID.randomUUID().toString());
        event.setName("JavaZone 2009");
        EventId eventId = EventId.eventId(event.getId());

        TreeMap<String, Event> events = TreeMap.<String, Event>empty(Ord.stringOrd).set(event.getName(), event);
        emsWrapper.listEvents = P.p(events.values());
        emsWrapper.findEventByName = flip(Functions.<String, Event>TreeMap_get()).f(events);
        DefaultIncogitoApplication application = new DefaultIncogitoApplication(incogitoHome, null, emsWrapper);

        application.afterPropertiesSet();

        IncogitoConfiguration configuration = application.getConfiguration();

        assertEquals(2, configuration.eventConfigurations.get(eventId).some().labels.size());
        assertEquals(5, configuration.eventConfigurations.get(eventId).some().levels.size());

        Label actualMyLabel = configuration.eventConfigurations.get(eventId).some().labels.get("MyLabel").some();
        assertEquals("My label", actualMyLabel.displayName);
        assertEquals("MyLabel", actualMyLabel.id);
        assertEquals("MyLabel", actualMyLabel.emsId);

        Label actualRenamedLabel = configuration.eventConfigurations.get(eventId).some().labels.get("renamed-label").some();
        assertEquals("Renamed Label", actualRenamedLabel.displayName);
        assertEquals("renamed-label", actualRenamedLabel.id);
        assertEquals("Renamed label", actualRenamedLabel.emsId);
    }

    public void testFilteringOfEvents() throws Exception {
        EmsWrapper emsWrapper = new EmsWrapper(new RestEmsService(null));
        Event javaZone2008 = new Event();
        javaZone2008.setId(UUID.randomUUID().toString());
        javaZone2008.setName("JavaZone 2008");

        Event javaZone2009 = new Event();
        javaZone2009.setId(UUID.randomUUID().toString());
        javaZone2009.setName("JavaZone 2009");

        TreeMap<String, Event> emsEvents = TreeMap.<String, Event>empty(Ord.stringOrd).
            set(javaZone2008.getName(), javaZone2008).
            set(javaZone2009.getName(), javaZone2009);
        emsWrapper.listEvents = P.p(emsEvents.values());
        emsWrapper.findEventByName = flip(Functions.<String, Event>TreeMap_get()).f(emsEvents);

        DefaultIncogitoApplication application = new DefaultIncogitoApplication(incogitoHome, null, emsWrapper);
        application.afterPropertiesSet();

        OperationResult<List<no.java.incogito.domain.Event>> operationResult = application.getEvents();
        assertTrue(operationResult.isOk());

        List<no.java.incogito.domain.Event> events = operationResult.value();
        assertEquals(1, events.length());
    }
}
