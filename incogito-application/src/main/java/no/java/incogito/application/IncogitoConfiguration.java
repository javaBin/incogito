package no.java.incogito.application;

import fj.F;
import fj.P2;
import fj.data.List;
import static fj.data.List.nil;
import fj.data.Option;
import fj.data.TreeMap;
import no.java.incogito.domain.CssConfiguration;
import static no.java.incogito.domain.CssConfiguration.defaultCssConfiguration;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Room;
import static no.java.incogito.domain.Event.emptyLabelIconMap;
import no.java.incogito.domain.Level.LevelId;
import static no.java.incogito.Functions.compose;
import no.java.incogito.Functions;
import org.joda.time.LocalDate;
import org.joda.time.Interval;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoConfiguration {

    public static final List<EventConfiguration> emptyEventConfigurations = nil();
    public final String baseurl;
    public final CssConfiguration cssConfiguration;
    public final Option<String> frontPageText;
    public final Option<String> aboutText;
    public final List<EventConfiguration> eventConfigurations;

    public IncogitoConfiguration(String baseurl, CssConfiguration cssConfiguration, Option<String> frontPageText, Option<String> aboutText,
                                 List<EventConfiguration> eventConfigurations) {
        this.baseurl = baseurl;
        this.cssConfiguration = cssConfiguration;
        this.frontPageText = frontPageText;
        this.aboutText = aboutText;
        this.eventConfigurations = eventConfigurations;
    }

    public static class EventConfiguration {
        public final String name;
        public final Option<String> blurb;
        public final List<P2<LocalDate, DayConfiguration>> dayConfigurations;
        public List<Room> presentationRooms;
        public final List<Label> labels;
        public final TreeMap<String, Label> labelMap;
        public final TreeMap<LevelId, Level> levels;
        private final long timestamp;

        public EventConfiguration(String name, Option<String> blurb,
                                  List<P2<LocalDate, DayConfiguration>> dayConfigurations, List<Room> presentationRooms,
                                  List<Label> labels, TreeMap<LevelId, Level> levels, long timestamp) {
            this.name = name;
            this.blurb = blurb;
            this.dayConfigurations = dayConfigurations;
            this.presentationRooms = presentationRooms;
            this.labels = labels;
            this.levels = levels;
            this.timestamp = timestamp;

            labelMap = labels.foldLeft(Functions.<String, Label>TreeMap_set().f(Label.id_), emptyLabelIconMap);
        }

        public static final F<EventConfiguration, String> name_ = new F<EventConfiguration, String>() {
            public String f(EventConfiguration eventConfiguration) {
                return eventConfiguration.name;
            }
        };

        public boolean isOutdated(long fileTimestamp) {
            return fileTimestamp > timestamp;
        }
    }

    public static class DayConfiguration {
        public final List<Room> rooms;
        public final List<Interval> timeslots;

        public DayConfiguration(List<Room> rooms, List<Interval> timeslots) {
            this.rooms = rooms;
            this.timeslots = timeslots;
        }
    }

    public String getBaseurl() {
        return baseurl;
    }
    
    public String getFrontPageText() {
        return frontPageText.orSome((String) null);
    }
    
    public String getAboutText() {
    	return aboutText.orSome((String) null);
    }

    public Option<EventConfiguration> findEventConfigurationByName(String name) {
        return eventConfigurations.find(compose(Functions.equals.f(name), EventConfiguration.name_));
    }

    public static final IncogitoConfiguration unconfigured = new IncogitoConfiguration("http://unconfigured",
        defaultCssConfiguration,Option.<String>none(), Option.<String>none(), emptyEventConfigurations);
}
