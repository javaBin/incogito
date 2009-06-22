package no.java.incogito.application;

import junit.framework.TestCase;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.UserId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.SessionId;
import static no.java.incogito.domain.User.createTransientUser;
import no.java.incogito.voldemort.LoggingConfiguration;
import no.java.incogito.voldemort.IncogitoServer;
import voldemort.client.MockStoreClientFactory;
import voldemort.client.StoreClientFactory;
import voldemort.serialization.StringSerializer;
import voldemort.serialization.json.JsonTypeSerializer;

import java.util.Map;
import java.io.File;

import fj.data.Option;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoIntegrationTest extends TestCase {
    
    private static final SessionId sessionA = new SessionId("session-a");
    private static final SessionId sessionB = new SessionId("session-b");

    IncogitoServer server;

    IncogitoApplication application;

    StoreClientFactory storeClientFactory = new MockStoreClientFactory<String, Object>(new StringSerializer("utf8"),
        new JsonTypeSerializer(UserClient.SCHEMA));

    public void setUp() {
        File home = new File(System.getProperty("basedir"), "src/test/resources/cluster-it/node-it");
        LoggingConfiguration loggingConfiguration = LoggingConfiguration.getInstance(home);

        server = loggingConfiguration.createIncogitoServer(home);

        server.start();

        UserClient userClient = new UserClient(storeClientFactory.<String, Map>getStoreClient("user"));
        application = new VoldemortIncogitoApplication(userClient);
    }

    public void tearDown() {
        storeClientFactory.close();
        server.stop();
    }

    public void testBasic() {
        UserId userId = new UserId("trygvis");

        Option<User> userOption = application.getUser(userId);

        if(userOption.isNone()){
            User user = createTransientUser(userId);
            user = user.markAttendance(sessionA);
            user = user.markInterest(sessionB);

            application.setUser(user);
        }

        userOption = application.getUser(userId);

        assertTrue(userOption.isSome());
        assertEquals(userId, userOption.some().id);
        assertEquals(2, userOption.some().sessionAssociations.length());

        Option<Schedule> scheduleOption = application.getSchedule(userId);

        assertTrue(scheduleOption.isSome());
    }
}
