package no.java.incogito.application;

import fj.F;
import fj.F2;
import static fj.Function.curry;
import fj.P;
import fj.data.Either;
import fj.data.List;
import fj.data.TreeMap;
import fj.pre.Ord;
import junit.framework.TestCase;
import no.java.ems.external.v2.*;
import no.java.incogito.ems.client.EmsWrapper;
import no.java.incogito.util.TestPathFactoryBean;
import org.apache.commons.httpclient.*;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.client.*;

import java.io.File;
import java.util.UUID;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultIncogitoApplicationTest extends TestCase {

    private File incogitoHome;

    public final static F<TreeMap<String, EventV2>, F<String, Either<String, EventV2>>> mockFindEventByName = curry(new F2<TreeMap<String, EventV2>, String, Either<String, EventV2>>() {
        public Either<String, EventV2> f(TreeMap<String, EventV2> events, String s) {
            return events.get(s).toEither("Could not find event '" + s + "'.");
        }
    });

    protected void setUp() throws Exception {
        incogitoHome = getIncogitoHome();
    }

    public static File getIncogitoHome() throws Exception {
        TestPathFactoryBean testPathFactoryBean = new TestPathFactoryBean();
        testPathFactoryBean.setTestClass(DefaultIncogitoApplication.class);
        testPathFactoryBean.setPath("src/test/resources/configuration-loading");
        return (File) testPathFactoryBean.getObject();
    }

    public void testFilteringOfEvents() throws Exception {
        HTTPCache cache = new HTTPCache(
            new MemoryCacheStorage(),
            new HTTPClientResponseResolver(new HttpClient(new MultiThreadedHttpConnectionManager())));
        EmsWrapper emsWrapper = new EmsWrapper(new RESTfulEmsV2Client(cache));
        EventV2 javaZone2008 = new EventV2();
        javaZone2008.setUuid(UUID.randomUUID().toString());
        javaZone2008.setName("JavaZone 2008");

        EventV2 javaZone2009 = new EventV2();
        javaZone2009.setUuid(UUID.randomUUID().toString());
        javaZone2009.setName("JavaZone 2009");

        TreeMap<String, EventV2> emsEvents = TreeMap.<String, EventV2>empty(Ord.stringOrd).
            set(javaZone2008.getName(), javaZone2008).
            set(javaZone2009.getName(), javaZone2009);
        emsWrapper.getEvents = P.p(emsEvents.values());
        emsWrapper.findEventByName = mockFindEventByName.f(emsEvents);

        DefaultIncogitoApplication application = new DefaultIncogitoApplication(incogitoHome, null, emsWrapper);
        application.afterPropertiesSet();

        OperationResult<List<no.java.incogito.domain.Event>> operationResult = application.getEvents();
        assertTrue(operationResult.isOk());

        List<no.java.incogito.domain.Event> events = operationResult.value();
        assertEquals(1, events.length());
    }
}
