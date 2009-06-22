package no.java.incogito.voldemort;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LoggingConfiguration {

    private static LoggingConfiguration instance;

    public static LoggingConfiguration getInstance(File home) {

        if (instance != null) {
            return instance;
        }

        File log4jProperties = new File(home, "config/log4j.properties").getAbsoluteFile();
        if (!log4jProperties.canRead()) {
            throw new RuntimeException("Can't read log4j configuration: " + log4jProperties);
        }

        PropertyConfigurator.configure(log4jProperties.getPath());

        return instance = new LoggingConfiguration();
    }

    public IncogitoServer createIncogitoServer(File home) {
        return new IncogitoServer(home);
    }
}
