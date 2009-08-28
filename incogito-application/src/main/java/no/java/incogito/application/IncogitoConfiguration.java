package no.java.incogito.application;

import fj.data.TreeMap;
import fj.pre.Ord;
import no.java.incogito.domain.CssConfiguration;
import static no.java.incogito.domain.CssConfiguration.defaultCssConfiguration;
import no.java.incogito.domain.Event.EventId;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoConfiguration {

    public static final TreeMap<EventId, String> emptyWelcomeTexts = TreeMap.empty(EventId.ord);
    public static final TreeMap<String, Label> emptyLabelMap = TreeMap.empty(Ord.stringOrd);
    public static final TreeMap<LevelId, Level> emptyLevelMap = TreeMap.empty(LevelId.ord);

    public final String baseurl;
    public final TreeMap<EventId, String> welcomeTexts;
    public final TreeMap<EventId, TreeMap<String, Label>> labels;
    public final TreeMap<EventId, TreeMap<LevelId, Level>> levels;
    public final CssConfiguration cssConfiguration;

    public IncogitoConfiguration(String baseurl, TreeMap<EventId, String> welcomeTexts,
                                 TreeMap<EventId, TreeMap<String, Label>> labels,
                                 TreeMap<EventId, TreeMap<LevelId, Level>> levels,
                                 CssConfiguration cssConfiguration) {
        this.baseurl = baseurl;
        this.welcomeTexts = welcomeTexts;
        this.labels = labels;
        this.levels = levels;
        this.cssConfiguration = cssConfiguration;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public static final IncogitoConfiguration unconfigured = new IncogitoConfiguration("http://unconfigured",
            emptyWelcomeTexts, TreeMap.<EventId, TreeMap<String, Label>>empty(EventId.ord),
                TreeMap.<EventId, TreeMap<LevelId, Level>>empty(EventId.ord), defaultCssConfiguration);
}
