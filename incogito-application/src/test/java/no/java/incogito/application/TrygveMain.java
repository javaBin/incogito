package no.java.incogito.application;

import fj.F;
import fj.data.List;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.versioning.TimeBasedInconsistencyResolver;

import java.util.Map;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TrygveMain {
    public static void main(String[] args) {

        System.out.println("UserClient.SCHEMA");
        System.out.println(UserClient.SCHEMA);

        SocketStoreClientFactory clientFactory = new SocketStoreClientFactory("tcp://localhost:6660");

        StoreClient<String, Map> client = clientFactory.getStoreClient("user", new TimeBasedInconsistencyResolver<Map>());

        UserClient userClient = new UserClient(client);
        IncogitoApplication incogito = new DefaultIncogitoApplication(userClient, null);

        List<UserId> userIds = List.range(1, 1000).map(toString).map(UserId.fromString);

        // Insert users
//        for (UserId userId : userIds) {
//
//            User user = User.createPristineUser(userId);
//
//            incogito.setUser(user);
//        }

        for (UserId userId : userIds) {
            OperationResult<User> userOperationResult = incogito.getUser(userId);
            System.out.println("userOperationResult = " + userOperationResult);
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
