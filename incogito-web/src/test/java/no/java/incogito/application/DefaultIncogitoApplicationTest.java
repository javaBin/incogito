package no.java.incogito.application;

import fj.Function;
import static fj.data.Option.some;
import junit.framework.TestCase;
import no.java.ems.client.RestEmsService;
import no.java.ems.domain.Event;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.ems.client.EmsWrapper;
import no.java.incogito.util.TestPathFactoryBean;

import java.io.File;
import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultIncogitoApplicationTest extends TestCase {
    public void testReconfigure() throws Exception {
        TestPathFactoryBean testPathFactoryBean = new TestPathFactoryBean();
        testPathFactoryBean.setTestClass(DefaultIncogitoApplication.class);
        testPathFactoryBean.setPath("src/test/resources/cluster-a/node-a");
        File incogitoHome = (File) testPathFactoryBean.getObject();
        EmsWrapper emsWrapper = new EmsWrapper(new RestEmsService(null));
        Event event = new Event();
        event.setId(UUID.randomUUID().toString());
        EventId eventId = EventId.eventId(event.getId());

        emsWrapper.findEventByName = Function.constant(some(event));
        DefaultIncogitoApplication application = new DefaultIncogitoApplication(incogitoHome, null, emsWrapper);

        application.afterPropertiesSet();

        IncogitoConfiguration configuration = application.getConfiguration();

        assertEquals(8, configuration.getLabelIcons(eventId).size());
        assertEquals(5, configuration.getLevelIcons(eventId).size());
    }
}
