package no.java.incogito.voldemort;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TestServer {
    public static void main(String[] args) {
        File basedir = new File(System.getProperty("basedir")).getAbsoluteFile();
        System.out.println("basedir = " + basedir);

        String instance = System.getProperty("instance");
        System.out.println("instance = " + instance);

        File instanceHome = new File(basedir, "src/test/resources/" + instance);
        LoggingConfiguration loggingConfiguration = LoggingConfiguration.getInstance(instanceHome);
        IncogitoServer server = loggingConfiguration.createIncogitoServer(instanceHome);

        server.start();

        final AtomicBoolean stopFlag = new AtomicBoolean();
        final Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.err.println("Shutting down...");
                stopFlag.set(true);
                mainThread.interrupt();
            }
        }, "Shutdown hook"));

        while (!stopFlag.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }

        server.stop();
        System.err.println("Shut down complete");
        System.exit(0);
    }
}
