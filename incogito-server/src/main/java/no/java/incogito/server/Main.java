package no.java.incogito.server;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("Main.main");
        File home = new File(args[0]).getAbsoluteFile();

        if (!home.isDirectory()) {
            System.err.println("Not a valid configuration directory: " + home.getAbsolutePath());
            System.exit(-1);
        }

        IncogitoServer server = new IncogitoServer(home);

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
