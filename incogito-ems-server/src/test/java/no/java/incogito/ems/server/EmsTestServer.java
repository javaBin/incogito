package no.java.incogito.ems.server;

import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
import no.java.ems.server.EmsServices;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsTestServer {
    public static void main(String[] args) throws Exception {
        File basedir = getBasedir(args);

        File emsHome = new File(basedir, "target/ems-home");

        System.setProperty("ems.home", emsHome.getAbsolutePath());

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

        EmsServices emsServices = (EmsServices) context.getBean("emsServices");

        List<Event> list = emsServices.getEventDao().getEvents();
        System.out.println("Events: (" + list.size() + "):");
        for (Event event : list) {
            System.out.println(" * " + event.getName() + " has " + emsServices.getSessionDao().getSessionIdsByEventId(event.getId()).size() + " sessions");
        }

        for (Event event : list) {
            if(!"JavaZone 2009".equals(event.getName())) {
                continue;
            }

            System.out.println("Id: " + event.getId());
            for (Session session : emsServices.getSessionDao().getSessions(event.getId())) {
                System.out.println("session = " + session.getTitle() + ", tags:" + StringUtils.join(session.getTags().toArray(), ", "));
            }
        }

        final AtomicBoolean shutdown = new AtomicBoolean();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown.set(true);
            }
        });

        while (!shutdown.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }

        System.out.println("Shutting down");
    }

    public static File getBasedir(String[] args) {
        File basedir;
        if(args.length > 0) {
            basedir = new File(args[0]);
        }
        else {
            basedir = new File("").getAbsoluteFile();
        }
        return basedir;
    }
}
