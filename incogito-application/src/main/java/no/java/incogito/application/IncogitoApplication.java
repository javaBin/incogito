package no.java.incogito.application;

import fj.Unit;
import fj.data.List;
import fj.data.Option;
import no.java.incogito.domain.Event;
import no.java.incogito.domain.Schedule;
import no.java.incogito.domain.Session;
import no.java.incogito.domain.SessionId;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.*;
import no.java.incogito.domain.UserSessionAssociation.InterestLevel;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface IncogitoApplication {
    IncogitoConfiguration getConfiguration();

    void reloadConfiguration() throws Exception;

    OperationResult<List<Event>> getEvents();

    OperationResult<Event> getEventByName(String eventName);

    OperationResult<List<Session>> getSessions(String eventName);

    OperationResult<Session> getSession(String eventName, SessionId sessionId);

    OperationResult<Session> getSessionByTitle(String eventName, String sessionTitle);

    OperationResult<User> createUser(User user);

    OperationResult<Unit> removeUser(UserId userId);

    OperationResult<User> getUser(UserId userId);

    OperationResult<Schedule> getSchedule(String eventName, String userId);

    OperationResult<Schedule> getSchedule(String eventName, Option<String> userId);

    OperationResult<User> setInterestLevel(String userName, String eventName, SessionId sessionId, InterestLevel interestLevel);

    OperationResult<byte[]> getSpeakerPhotoForSession(String sessionId, int index);
}
