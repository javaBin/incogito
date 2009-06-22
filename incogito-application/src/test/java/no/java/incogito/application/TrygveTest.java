package no.java.incogito.application;

import fj.F;
import fj.data.List;
import fj.data.Option;
import no.java.incogito.domain.User;
import no.java.incogito.domain.UserId;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.versioning.TimeBasedInconsistencyResolver;

import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TrygveTest {
    public static void main(String[] args) {

        System.out.println("UserClient.SCHEMA");
        System.out.println(UserClient.SCHEMA);

        SocketStoreClientFactory clientFactory = new SocketStoreClientFactory("tcp://localhost:6660");

        StoreClient<String, Map> client = clientFactory.getStoreClient("user", new TimeBasedInconsistencyResolver<Map>());

        UserClient userClient = new UserClient(client);
        IncogitoApplication application = new VoldemortIncogitoApplication(userClient);

        List<UserId> userIds = List.range(1, 1000).map(toString).map(UserId.fromString);

        // Insert users
//        for (UserId userId : userIds) {
//
//            User user = User.createTransientUser(userId);
//
//            application.setUser(user);
//        }

        for (UserId userId : userIds) {
            Option<User> option = application.getUser(userId);
            System.out.println("option.isNone() = " + option.isNone());
        }


        System.out.println("Done");
        clientFactory.close();
    }

    static final F<Integer, String> toString = new F<Integer, String>() {
        public String f(Integer integer) {
            return integer.toString();
        }
    };
}
