package no.java.incogito.application;

import no.java.ems.server.EmsServices;
import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
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

    public DataGenerator(EmsServices services) {
        this.eventDao = services.getEventDao();
        this.sessionDao = services.getSessionDao();
    }

    public void generate1() {
        for (Event event : eventDao.getEvents()) {
            for (String sessionId : sessionDao.getSessionIdsByEventId(event.getId())) {
                System.out.println("Deleting session #" + sessionId);
                sessionDao.deleteSession(sessionId);
            }
            System.out.println("Deleting event #" + event.getId());
            eventDao.deleteEvent(event.getId());
        }

        for (String name : List.list("JavaZone 2006", "JavaZone 2007", "JavaZone 2008", "JavaZone 2009")) {
            Event e = new Event();
            e.setName(name);
            eventDao.saveEvent(e);

            for (Integer integer : Stream.range(0, 10)) {
                Session session = new Session();
                session.setTitle(name + ", Session #" + integer);
                session.setEventId(e.getId());
                sessionDao.saveSession(session);
            }
        }
    }
}
