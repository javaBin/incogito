package no.java.incogito;

import fj.data.List;
import static fj.data.List.list;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserId;
import no.java.incogito.voldemort.IncogitoServer;
import no.java.incogito.voldemort.Log4jConfiguration;
import voldemort.client.StoreClient;
import voldemort.versioning.Versioned;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TrygveTest {

    private final static File homeA = new File("incogito-server/src/test/resources/node-a");
    private final static File homeB = new File("incogito-server/src/test/resources/node-b");
    private final static File homeC = new File("incogito-server/src/test/resources/node-c");

    public static void main(String[] args) {
        Log4jConfiguration log4jConfiguration = Log4jConfiguration.getInstance(homeA);
        IncogitoServer serverA = log4jConfiguration.createIncogitoServer(homeA);
        IncogitoServer serverB = log4jConfiguration.createIncogitoServer(homeB);
        IncogitoServer serverC = log4jConfiguration.createIncogitoServer(homeC);

        System.out.println("Starting server A");
        serverA.start();

        System.out.println("Starting server B");
        serverB.start();

        System.out.println("Starting server C");
        serverC.start();

        StoreClient<String, User> userClient = serverA.getStoreClientFactory().getStoreClient("user");

        List<UserId> range = list("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m").map(UserId.fromString);

        System.out.println("faen");
        for (Map.Entry entry : userClient.getAll(range.map(UserId.toString)).entrySet()) {
            System.out.println("entry.getKey() = " + entry.getKey());
            System.out.println("entry.getValue() = " + entry.getValue());
        }

        System.out.println("Dumping 1");
        for (Map.Entry<String, Versioned<User>> entry : userClient.getAll(range.map(UserId.toString)).entrySet()) {
            System.out.println("entry.getKey() = " + entry.getKey());
            System.out.println("entry.getValue().getValue() = " + entry.getValue().getValue());
        }

        System.out.println("Creating");
        for (UserId userId : range) {
            Versioned<User> versionedUser = userClient.get(userId.value);

            System.out.println("versionedUser = " + versionedUser);
            if (versionedUser == null) {
                userClient.put(userId.value, User.createPersistentUser(userId));
            }
        }

        System.out.println("Dumping 2");
        for (Map.Entry<String, Versioned<User>> entry : userClient.getAll(range.map(UserId.toString)).entrySet()) {
            System.out.println("entry.getKey() = " + entry.getKey());
            System.out.println("entry.getValue().getValue().id = " + entry.getValue().getValue());
        }

        serverA.stop();
        serverB.stop();
        serverC.stop();

        System.exit(0);
    }
}
