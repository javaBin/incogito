package no.java.incogito.application;

import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.UserId;
import no.java.incogito.domain.User;
import fj.data.Option;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface IncogitoApplication {
    Option<User> getUser(UserId userId);

    Option<Schedule> getSchedule(UserId id);

    void setUser(User user);
}
