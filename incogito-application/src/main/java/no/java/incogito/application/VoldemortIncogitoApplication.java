package no.java.incogito.application;

import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.UserId;
import no.java.incogito.domain.User;
import fj.data.Option;
import fj.F;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VoldemortIncogitoApplication implements IncogitoApplication {

    private final UserClient userClient;

    public VoldemortIncogitoApplication(UserClient userClient) {
        this.userClient = userClient;
    }

    public Option<User> getUser(UserId userId) {
        return userClient.getUser(userId);
    }

    public Option<Schedule> getSchedule(UserId id) {
        return userClient.getUser(id).map(new F<User, Schedule>() {
            public Schedule f(User user) {
                return new Schedule();
            }
        });
    }

    public void setUser(User user) {
        userClient.setUser(user);
    }
}
