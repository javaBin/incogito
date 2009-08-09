package no.java.incogito.web.servlet;

import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.domain.Event;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoIconFunctions {
    public static String level(IncogitoApplication application, Event event, String level) {
        // TODO: This should come from the Event object
        return application.getConfiguration().baseurl + "/rest/events/" + event.name + "/icons/levels/" + level + ".png";
    }

    public static String label(IncogitoApplication application, Event event, String label) {
        // TODO: This should come from the Event object
        return application.getConfiguration().baseurl + "/rest/events/" + event.name + "/icons/labels/" + label + ".png";
    }
}
