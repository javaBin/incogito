package no.java.incogito.application;

import no.java.incogito.domain.Event.EventId;
import fj.data.TreeMap;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoConfiguration {

    public final String baseurl;
    public final TreeMap<EventId, String> welcomeTexts;

    public IncogitoConfiguration(String baseurl, TreeMap<EventId, String> welcomeTexts) {
        this.baseurl = baseurl;
        this.welcomeTexts = welcomeTexts;
    }

    public String getBaseurl() {
        return baseurl;
    }
}
