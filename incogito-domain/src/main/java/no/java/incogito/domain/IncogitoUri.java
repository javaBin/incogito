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
        return new IncogitoEventsUri(uriBuilder.clone().path("events"));
    }

    public String toString() {
        return uriBuilder.build().toString();
    }

    public static class IncogitoEventsUri {
        private final UriBuilder events;

        private IncogitoEventsUri(UriBuilder events) {
            this.events = events;
        }

        public String toString() {
            return events.build().toString();
        }

        public IncogitoEventUri eventUri(String event) {
            return new IncogitoEventUri(events.clone().segment(event));
        }

        public static class IncogitoEventUri {
            private final UriBuilder event;

            private IncogitoEventUri(UriBuilder event) {
                this.event = event;
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

            public IncogitoSessionsUri sessions() {
                return new IncogitoSessionsUri(this, event.clone().segment("sessions"));
            }

            public static class IncogitoSessionsUri {
                public final IncogitoEventUri eventUri;
                private final UriBuilder sessions;

                public IncogitoSessionsUri(IncogitoEventUri eventUri, UriBuilder sessions) {
                    this.eventUri = eventUri;
                    this.sessions = sessions;
                }

                public String toString() {
                    return sessions.build().toString();
                }

                public String session(Session session) {
                    return sessions.clone().segment(session.title).toString();
                }
            }
        }
    }
}
