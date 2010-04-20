package no.java.incogito.ems.server;

import no.java.ems.dao.*;
import no.java.ems.server.*;
import no.java.ems.server.domain.*;
import org.slf4j.bridge.*;
import org.springframework.context.support.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

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
        thread.join();
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
        emsHome.mkdirs();

        System.setProperty("java.security.auth.login.config", new File(basedir, "src/test/resources/login.conf").getAbsolutePath());
        System.setProperty("ems.home", emsHome.getAbsolutePath());

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:incogito-ems-server-applicationContext-test.xml");

        context.getBean("derbyService", DerbyService.class);
        EventDao eventDao = (EventDao) context.getBean("jdbcTemplateEventDao", EventDao.class);
        SessionDao sessionDao = (SessionDao) context.getBean("jdbcTemplateSessionDao", SessionDao.class);

        List<Event> list = eventDao.getEvents();
        System.out.println("Events: (" + list.size() + "):");
        for (Event event : list) {
            System.out.println(" * " + event.getName() + " has " + sessionDao.getSessionIdsByEventId(event.getId()).size() + " sessions");
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
