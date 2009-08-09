package no.java.incogito.application;

import fj.data.TreeMap;
import no.java.incogito.domain.CssConfiguration;
import static no.java.incogito.domain.CssConfiguration.defaultCssConfiguration;
import no.java.incogito.domain.Event.EventId;
import static no.java.incogito.domain.Event.emptyLabelIconMap;
import static no.java.incogito.domain.Event.emptyLevelIconMap;
import no.java.incogito.domain.Session.Level;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoConfiguration {

    public static final TreeMap<EventId, String> emptyWelcomeTexts = TreeMap.empty(EventId.ord);
    public static final TreeMap<EventId, TreeMap<String, File>> emptyLabelIconMaps = TreeMap.empty(EventId.ord);
    public static final TreeMap<EventId, TreeMap<Level, File>> emptyLevelIconMaps = TreeMap.empty(EventId.ord);

    public final String baseurl;
    public final TreeMap<EventId, String> welcomeTexts;
    public final TreeMap<EventId, TreeMap<Level, File>> levelIcons;
    public final TreeMap<EventId, TreeMap<String, File>> labelIcons;
    public final CssConfiguration cssConfiguration;

    public IncogitoConfiguration(String baseurl, TreeMap<EventId, String> welcomeTexts, TreeMap<EventId, TreeMap<Level, File>> levelIcons, TreeMap<EventId, TreeMap<String, File>> labelIcons, CssConfiguration cssConfiguration) {
        this.baseurl = baseurl;
        this.welcomeTexts = welcomeTexts;
        this.levelIcons = levelIcons;
        this.labelIcons = labelIcons;
        this.cssConfiguration = cssConfiguration;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public TreeMap<Level, File> getLevelIcons(EventId eventId) {
        return levelIcons.get(eventId).orSome(emptyLevelIconMap);
    }

    public TreeMap<String, File> getLabelIcons(EventId eventId) {
        return labelIcons.get(eventId).orSome(emptyLabelIconMap);
    }

    public static IncogitoConfiguration unconfigured() {
        return new IncogitoConfiguration("http://unconfigured", emptyWelcomeTexts, emptyLevelIconMaps, emptyLabelIconMaps, defaultCssConfiguration);
    }
}
