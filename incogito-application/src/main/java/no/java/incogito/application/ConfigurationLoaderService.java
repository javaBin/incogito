package no.java.incogito.application;

import fj.F;
import fj.F2;
import static fj.Function.compose;
import fj.P;
import fj.P1;
import fj.control.parallel.Callables;
import fj.data.List;
import static fj.data.List.list;
import static fj.data.List.nil;
import fj.data.Option;
import static fj.data.Option.some;
import fj.data.TreeMap;
import fj.pre.Ord;
import no.java.incogito.Functions;
import no.java.incogito.IO;
import static no.java.incogito.IO.Strings.streamToString;
import no.java.incogito.PropertiesF;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;
import no.java.incogito.domain.CssConfiguration;
import static no.java.incogito.domain.Event.emptyLabelIconMap;
import static no.java.incogito.domain.Event.emptyLevelIconMap;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.ems.client.EmsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * TODO: This should take an existing configuration as an argument and use that if none of the
 * resources on disk have changed.
 *
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component
public class ConfigurationLoaderService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EmsWrapper emsWrapper;

    @Autowired
    public ConfigurationLoaderService(EmsWrapper emsWrapper) {
        this.emsWrapper = emsWrapper;
    }

    public IncogitoConfiguration loadConfiguration(File incogitoHome, IncogitoConfiguration existingConfiguration) throws Exception {
        File props = new File(incogitoHome, "etc/incogito.properties").getAbsoluteFile();
//        logger.info("Reloading configuration from: " + props);
        File etc = props.getParentFile();

        TreeMap<String, String> properties = IO.<TreeMap<String, String>>runFileInputStream_().
            f(PropertiesF.loadPropertiesAsMap).
            f(props).call();

        String baseurl = properties.get("baseurl").toEither("").right().valueE(P.p("Missing required property: 'baseurl'"));
        String eventsConfiguration = properties.get("events").toEither("").right().valueE(P.p("Missing required property: 'events'"));

        F<String, Option<Double>> parseDouble = compose(Functions.<NumberFormatException, Double>Either_rightToOption_(), Functions.parseDouble);

        double sessionEmStart = properties.get("sessionEmStart").bind(parseDouble).orSome(CssConfiguration.defaultCssConfiguration.sessionEmStart);
        double emPerMinute = properties.get("emPerMinute").bind(parseDouble).orSome(CssConfiguration.defaultCssConfiguration.emPerMinute);
        double emPerRoom = properties.get("emPerRoom").bind(parseDouble).orSome(CssConfiguration.defaultCssConfiguration.emPerRoom);

        CssConfiguration cssConfiguration = new CssConfiguration(sessionEmStart, emPerMinute, emPerRoom);

        File eventsDirectory = new File(etc, "events");

        List<EventConfiguration> events = nil();

        // TODO: Check for icons on disk and put those in a map, including alternative texts for each image
        // TODO: Load the order of the rooms
        // TODO: Load "extra" room sessions which are special like "lunch" and "party zone"
        // TODO: Consider switching to reading a <event id>.xml file if it exist and use that as configuration
        for (final String eventName : list(eventsConfiguration.split(",")).map(Functions.trim)) {

            final File eventDirectory = new File(eventsDirectory, eventName);

            if (!eventDirectory.isDirectory()) {
                logger.warn("Missing configuration for event '" + eventName + "'.");
            }

            File eventPropertiesFile = new File(eventDirectory, "event.properties");

            Option<EventConfiguration> existingEventConfiguration = existingConfiguration.findEventConfigurationByName(eventName);
            if(existingEventConfiguration.isSome() && !existingEventConfiguration.some().isOutdated(eventPropertiesFile.lastModified())) {
                events = events.cons(existingEventConfiguration.some());
                continue;
            }

            logger.warn("Reloading configuration for event '" + eventName + "'");

            final TreeMap<String, String> eventProperties = Callables.option(IO.<TreeMap<String, String>>runFileInputStream_().
                f(PropertiesF.loadPropertiesAsMap).
                f(eventPropertiesFile))._1().orSome(TreeMap.<String, String>empty(Ord.stringOrd));

            Option<String> frontPageContent = some(new File(eventDirectory, "frontpage.txt")).
                filter(Functions.File_canRead).
                map(IO.<String>runFileInputStream_().f(streamToString)).
                bind(compose(P1.<Option<String>>__1(), Callables.<String>option()));

            Option<String> blurb = eventProperties.get("blurb");

            TreeMap<String, Label> labelMap = List.list(eventProperties.get("labels").orSome("").split(",")).
                map(Functions.trim).
                foldLeft(new F2<TreeMap<String, Label>, String, TreeMap<String, Label>>() {
                    public TreeMap<String, Label> f(TreeMap<String, Label> labelIcons, String emsId) {
                        // If the configuration file contain an ".id" element, use that as the internal id
                        String id = eventProperties.get(emsId + ".id").orSome(emsId);

                        if (id.indexOf(' ') != -1) {
                            logger.warn("Invalid id for ems label '" + emsId + "'. Override the id by adding a '" + emsId + ".id' property to the event configuration.");
                            return labelIcons;
                        }

                        File iconFile = new File(eventDirectory, "labels/" + id + ".png");

                        Option<Label> label = some(id).bind(some(emsId), eventProperties.get(emsId + ".displayName"), Option.iif(Functions.File_canRead, iconFile), Label.label_);

                        if (label.isSome()) {
                            return labelIcons.set(label.some().id, label.some());
                        }

                        logger.warn("Could not find file for label: " + id);

                        return labelIcons;
                    }
                }, emptyLabelIconMap);

            TreeMap<LevelId, Level> levelMap = List.list(eventProperties.get("levels").orSome("").split(",")).
                map(Functions.trim).
                foldLeft(new F2<TreeMap<LevelId, Level>, String, TreeMap<LevelId, Level>>() {
                    public TreeMap<LevelId, Level> f(TreeMap<LevelId, Level> levelIcons, String id) {
                        File iconFile = new File(eventDirectory, "levels/" + id + ".png");

                        Option<Level> level = LevelId.valueOf_.f(id).bind(eventProperties.get(id + ".displayName"), Option.iif(Functions.File_canRead, iconFile), Level.level_);

                        if (level.isSome()) {
                            return levelIcons.set(level.some().id, level.some());
                        }
                        return levelIcons;
                    }
                }, emptyLevelIconMap);

            Option<no.java.ems.domain.Event> eventOption = emsWrapper.findEventByName.f(eventName);

            if (eventOption.isNone()) {
                logger.warn("Could not find event '{}' in EMS.", eventName);
                continue;
            }

            events = events.cons(new EventConfiguration(eventName, blurb, frontPageContent, labelMap, levelMap,
                    eventPropertiesFile.lastModified()));
        }

        return new IncogitoConfiguration(baseurl, cssConfiguration, events.reverse());
    }
}
