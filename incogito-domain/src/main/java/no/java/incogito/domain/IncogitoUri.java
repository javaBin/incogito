package no.java.incogito.domain;

import javax.ws.rs.core.UriBuilder;
import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoUri {
    private final UriBuilder uriBuilder;

    public IncogitoUri(UriBuilder baseurl) {
        this.uriBuilder = baseurl.clone().segment("rest");
    }

    public IncogitoUri(String baseurl) {
        this(fromUri(baseurl));
    }

    public IncogitoEventsUri events() {
        return new IncogitoEventsUri(this, uriBuilder.clone().path("events"));
    }

    public String personImage(String personId) {
        return uriBuilder.clone().segment("people", personId, "photo").build().toString();
    }

    public String toString() {
        return uriBuilder.build().toString();
    }

    public static class IncogitoEventsUri {
        public final IncogitoUri incogitoUri;
        private final UriBuilder events;

        public IncogitoEventsUri(IncogitoUri incogitoUri, UriBuilder events) {
            this.incogitoUri = incogitoUri;
            this.events = events;
        }

        public String toString() {
            return events.build().toString();
        }

        public IncogitoEventUri eventUri(String event) {
            return new IncogitoEventUri(this, events.clone().segment(event));
        }

        public static class IncogitoEventUri {
            public final IncogitoEventsUri eventsUri;
            private final UriBuilder event;
            private final UriBuilder sessions;

            private IncogitoEventUri(IncogitoEventsUri eventsUri, UriBuilder event) {
                this.eventsUri = eventsUri;
                this.event = event;
                sessions = event.clone().segment("sessions");
            }

            public String toString() {
                return event.build().toString();
            }

            public IncogitoLabelsIconUri labelsIcon() {
                return new IncogitoLabelsIconUri(event.clone().segment("icons", "labels"));
            }

            public static class IncogitoLabelsIconUri {
                private final UriBuilder labels;

                private IncogitoLabelsIconUri(UriBuilder labels) {
                    this.labels = labels;
                }

                public String toString() {
                    return labels.build().toString();
                }

                public String png(Label label) {
                    return labels.clone().segment(label.id + ".png").build().toString();
                }
            }

            public IncogitoLevelsIconUri levelsIcon() {
                return new IncogitoLevelsIconUri(event.clone().segment("icons", "levels"));
            }

            public static class IncogitoLevelsIconUri {
                private final UriBuilder levels;

                private IncogitoLevelsIconUri(UriBuilder levels) {
                    this.levels = levels;
                }

                public String toString() {
                    return levels.build().toString();
                }

                public String png(Level level) {
                    return levels.clone().segment(level.id.name() + ".png").build().toString();
                }
            }

            public String session(Session session) {
                return sessions.clone().segment(session.title).toString();
            }

            public String session(String sessionTitle) {
                return sessions.clone().segment(sessionTitle).toString();
            }

        }
    }
}
