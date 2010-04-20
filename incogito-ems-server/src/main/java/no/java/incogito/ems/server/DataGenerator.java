package no.java.incogito.ems.server;

import no.java.ems.server.domain.Event;
import no.java.ems.server.domain.Session;
import no.java.ems.dao.*;
import fj.data.List;
import fj.data.Stream;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DataGenerator {
    private final EventDao eventDao;
    private final SessionDao sessionDao;

    public DataGenerator(EventDao eventDao, SessionDao sessionDao) {
        this.eventDao = eventDao;
        this.sessionDao = sessionDao;
    }

    public DataSet1 generate1() {
        for (Event event : eventDao.getEvents()) {
            for (String sessionId : sessionDao.getSessionIdsByEventId(event.getId())) {
                System.out.println("Deleting session #" + sessionId);
                sessionDao.deleteSession(sessionId);
            }
            System.out.println("Deleting event #" + event.getId());
            eventDao.deleteEvent(event.getId());
        }

        return new DataSet1();
    }

    public class DataSet1 {
        public final Event javaZone2006;
        public final Event javaZone2007;
        public final Event javaZone2008;
        public final Event javaZone2009;

        public final List<Session> javaZone2006Sessions;
        public final List<Session> javaZone2007Sessions;
        public final List<Session> javaZone2008Sessions;
        public final List<Session> javaZone2009Sessions;

        private DataSet1() {
            javaZone2006 = generateEvent("JavaZone 2006");
            javaZone2006Sessions = generateSessions(javaZone2006);

            javaZone2007 = generateEvent("JavaZone 2007");
            javaZone2007Sessions = generateSessions(javaZone2007);

            javaZone2008 = generateEvent("JavaZone 2008");
            javaZone2008Sessions = generateSessions(javaZone2008);

            javaZone2009 = generateEvent("JavaZone 2009");
            javaZone2009Sessions = generateSessions(javaZone2009);
        }

        private Event generateEvent(String name) {
            Event e = new Event();
            e.setName(name);
            eventDao.saveEvent(e);

            return e;
        }

        private List<Session> generateSessions(Event event) {
            List<Session> sessions = List.nil();
            for (Integer integer : Stream.range(0, 10)) {
                Session session = new Session();
                session.setTitle(event.getName() + ", Session #" + integer);
                session.setEventId(event.getId());
                sessionDao.saveSession(session);
            }

            return sessions;
        }
    }
}
