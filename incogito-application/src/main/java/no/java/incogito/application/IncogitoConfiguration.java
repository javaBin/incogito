package no.java.incogito.application;

import fj.F;
import fj.data.List;
import static fj.data.List.nil;
import fj.data.Option;
import fj.data.TreeMap;
import no.java.incogito.domain.CssConfiguration;
import static no.java.incogito.domain.CssConfiguration.defaultCssConfiguration;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoConfiguration {

    public static class EventConfiguration {
        public final String name;
        public final Option<String> blurb;
        public final Option<String> frontPageText;
        public final TreeMap<String, Label> labels;
        public final TreeMap<LevelId, Level> levels;

        public EventConfiguration(String name, Option<String> blurb, Option<String> frontPageText, TreeMap<String, Label> labels, TreeMap<LevelId, Level> levels) {
            this.name = name;
            this.blurb = blurb;
            this.frontPageText = frontPageText;
            this.labels = labels;
            this.levels = levels;
        }

        public static final F<EventConfiguration, String> name_ = new F<EventConfiguration, String>() {
            public String f(EventConfiguration eventConfiguration) {
                return eventConfiguration.name;
            }
        };
    }

    public static final List<EventConfiguration> emptyEventConfigurations = nil();
    public final String baseurl;
    public final CssConfiguration cssConfiguration;

    public final List<EventConfiguration> eventConfigurations;

    public IncogitoConfiguration(String baseurl, CssConfiguration cssConfiguration,
                                 List<EventConfiguration> eventConfigurations) {
        this.baseurl = baseurl;
        this.cssConfiguration = cssConfiguration;
        this.eventConfigurations = eventConfigurations;
    }

    public String getBaseurl() {
        return baseurl;
    }

    public static final IncogitoConfiguration unconfigured = new IncogitoConfiguration("http://unconfigured",
        defaultCssConfiguration, emptyEventConfigurations);
}
