package no.java.incogito.voldemort;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Log4jConfiguration {

    private static Log4jConfiguration instance;

    public static Log4jConfiguration getInstance(File home) {

        if (instance != null) {
            return instance;
        }

        File log4jProperties = new File(home, "config/log4j.properties").getAbsoluteFile();
        if (!log4jProperties.canRead()) {
            throw new RuntimeException("Can't read log4j configuration: " + log4jProperties);
        }

        PropertyConfigurator.configure(log4jProperties.getPath());

        return instance = new Log4jConfiguration();
    }

    public IncogitoServer createIncogitoServer(File home) {
        return new IncogitoServer(home);
    }
}
