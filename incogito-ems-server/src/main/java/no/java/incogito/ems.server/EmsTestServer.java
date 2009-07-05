package no.java.incogito.ems.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;

import no.java.ems.server.EmsServices;
import no.java.ems.domain.Event;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsTestServer {
    public static void main(String[] args) throws Exception {
        File basedir = new File(args[0]);

        File emsHome = new File(basedir, "target/ems-home");

        System.setProperty("ems.home", emsHome.getAbsolutePath());

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");

        EmsServices emsServices = (EmsServices) context.getBean("emsServices");

        List<Event> list = emsServices.getEventDao().getEvents();
        System.out.println("Events: (" + list.size() + "):");
        for (Event event : list) {
            System.out.println("event = " + event.getName());
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
}
