package no.java.incogito.application;

import fj.F;
import fj.F2;
import static fj.Function.compose;
import fj.P1;
import fj.P2;
import fj.P;
import static fj.P.p;
import fj.control.parallel.Callables;
import fj.data.List;
import static fj.data.List.list;
import static fj.data.List.nil;
import fj.data.Option;
import static fj.data.Option.some;
import static fj.data.Option.none;
import static fj.data.Option.somes;
import fj.data.TreeMap;
import fj.data.Either;
import fj.pre.Ord;
import no.java.incogito.Functions;
import no.java.incogito.IO;
import static no.java.incogito.IO.Strings.streamToString;
import no.java.incogito.PropertiesF;
import static no.java.incogito.Functions.trim;
import static no.java.incogito.Functions.split;
import no.java.incogito.application.IncogitoConfiguration.EventConfiguration;
import no.java.incogito.application.IncogitoConfiguration.DayConfiguration;
import no.java.incogito.domain.CssConfiguration;
import static no.java.incogito.domain.Event.emptyLevelIconMap;
import no.java.incogito.domain.Label;
import no.java.incogito.domain.Level;
import no.java.incogito.domain.Room;
import no.java.incogito.domain.Level.LevelId;
import no.java.incogito.ems.client.EmsWrapper;
import no.java.ems.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.joda.time.LocalDate;
import org.joda.time.Interval;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

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

    DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().
            appendYear(4, 4).
            appendLiteral('-').
            appendMonthOfYear(2).
            appendLiteral('-').
            appendDayOfMonth(2).
            toFormatter();

    DateTimeFormatter timeslotFormatter = new DateTimeFormatterBuilder().
            appendHourOfDay(2).
            appendMinuteOfHour(2).
            toFormatter();

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

        String baseurl = properties.get("baseurl").toEither("").right().valueE(p("Missing required property: 'baseurl'"));
        String eventsConfiguration = properties.get("events").toEither("").right().valueE(p("Missing required property: 'events'"));

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
        for (final String eventName : list(eventsConfiguration.split(",")).map(trim)) {

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

            List<Label> labels = Option.somes(List.list(eventProperties.get("labels").orSome("").split(",")).
                    map(trim).
                    map(new F<String, Option<Label>>() {
                        public Option<Label> f(String emsId) {
                            // If the configuration file contain an ".id" element, use that as the internal id
                            String id = eventProperties.get(emsId + ".id").orSome(emsId);

                            if (id.indexOf(' ') != -1) {
                                logger.warn("Invalid id for ems label '" + emsId + "'. Override the id by adding a '" + emsId + ".id' property to the event configuration.");
                                return none();
                            }

                            File iconFile = new File(eventDirectory, "labels/" + id + ".png");

                            Option<Label> label = some(id).bind(some(emsId), eventProperties.get(emsId + ".displayName"), Option.iif(Functions.File_canRead, iconFile), Label.label_);

                            if (!label.isSome()) {
                                logger.warn("Could not find file for label: " + id);
                            }

                            return label;
                        }
                    }));

            TreeMap<LevelId, Level> levelMap = List.list(eventProperties.get("levels").orSome("").split(",")).
                map(trim).
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

            Either<String, Event> eventEither = emsWrapper.findEventByName.f(eventName);

            if (eventEither.isLeft()) {
                logger.warn("Could not find event '{}' in EMS: ", eventName, eventEither.left().value());
                continue;
            }

            List<Room> presentationRooms = eventProperties.get("room.presentation").
                    map(split.f(",")).orSome(List.<String>nil()).
                    map(compose(Room.room_, trim));

            List<P2<LocalDate, DayConfiguration>> days = eventProperties.get("dates").
                    map(split.f(",")).orSome(List.<String>nil()).
                    map(new F<String, LocalDate>() {
                        public LocalDate f(String s) {
                            return dateFormatter.parseDateTime(s.trim()).toLocalDate();
                        }
                    }).
                    map(new F<LocalDate, P2<LocalDate, DayConfiguration>>() {
                        public P2<LocalDate, DayConfiguration> f(LocalDate localDate) {
                            String roomsKey = dateFormatter.print(localDate) + ".rooms";
                            Option<String> roomsOption = eventProperties.get(roomsKey);
                            String timeslotsKey = dateFormatter.print(localDate) + ".timeslots";
                            Option<String> timeslotsOption = eventProperties.get(timeslotsKey);

                            List<Room> rooms = nil();
                            List<Interval> timeslots = nil();

                            if(roomsOption.isNone()) {
                                logger.warn("Missing room configuration: " + roomsKey);
                                return P.p(localDate, new DayConfiguration(rooms, timeslots));
                            }

                            if(timeslotsOption.isNone()) {
                                logger.warn("Missing timeslots configuration: " + timeslotsKey);
                                return P.p(localDate, new DayConfiguration(rooms, timeslots));
                            }

                            rooms = roomsOption.map(split.f(",")).orSome(List.<String>nil()).
                                    map(compose(Room.room_, trim));

                            final DateTime dateTime = localDate.toDateMidnight().toDateTime();

                            timeslots = somes(timeslotsOption.map(split.f(",")).orSome(List.<String>nil()).
                                    map(trim).
                                    map(new F<String, Option<Interval>>() {
                                        public Option<Interval> f(String s) {
                                            List<String> parts = split.f("-").f(s);

                                            if(parts.length() != 2) {
                                                logger.warn("Invalid timeslot: " + s);
                                                return none();
                                            }
                                            DateTime start = timeslotFormatter.parseDateTime(parts.index(0));
                                            DateTime end = timeslotFormatter.parseDateTime(parts.index(1));
                                            return some(new Interval(
                                                    dateTime.withHourOfDay(start.getHourOfDay()).withMinuteOfHour(start.getMinuteOfHour()),
                                                    dateTime.withHourOfDay(end.getHourOfDay()).withMinuteOfHour(end.getMinuteOfHour())));
                                        }
                                    }));

                            return P.p(localDate, new DayConfiguration(rooms, timeslots));
                        }
                    });

            if(days.isEmpty()) {
                logger.warn("Misconfiguration: missing a 'dates' property.");
                continue;
            }

            events = events.cons(new EventConfiguration(eventName, blurb, frontPageContent, days,
                    presentationRooms, labels, levelMap, eventPropertiesFile.lastModified()));
        }

        return new IncogitoConfiguration(baseurl, cssConfiguration, events.reverse());
    }
}
