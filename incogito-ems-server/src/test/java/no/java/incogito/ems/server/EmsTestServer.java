package no.java.incogito.ems.server;

import no.java.ems.domain.Event;
import no.java.ems.domain.Session;
import no.java.ems.server.EmsServices;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsTestServer {
    public static void main(final String[] args) throws Exception {
        // The thread name is so annoyingly long..

        Thread thread = new Thread(new Runnable() {
            public void run() {
                runTestServer(args);
            }
        }, "main");
        thread.setDaemon(false);
        thread.start();
        thread.wait();
    }

    public static void runTestServer(String[] args) {
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            rootLogger.removeHandler(handler);
        }
        SLF4JBridgeHandler.install();

        File basedir = getBasedir(args);

        File emsHome = new File(basedir, "target/ems-home");

        System.setProperty("ems.home", emsHome.getAbsolutePath());

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:incogito-ems-server-applicationContext-test.xml");

        EmsServices emsServices = (EmsServices) context.getBean("emsServices");

        List<Event> list = emsServices.getEventDao().getEvents();
        System.out.println("Events: (" + list.size() + "):");
        for (Event event : list) {
            System.out.println(" * " + event.getName() + " has " + emsServices.getSessionDao().getSessionIdsByEventId(event.getId()).size() + " sessions");
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
        context.stop();
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
