package no.java.incogito.domain;

import javax.ws.rs.core.UriBuilder;
import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoUri {
    private final UriBuilder rest;
    private final UriBuilder baseurl;

    public IncogitoUri(UriBuilder baseurl) {
        this.baseurl = baseurl.clone();
        this.rest = baseurl.clone().segment("rest");
    }

    public IncogitoUri(String baseurl) {
        this(fromUri(baseurl));
    }

    public IncogitoRestEventsUri restEvents() {
        return new IncogitoRestEventsUri(this, rest.clone().path("events"));
    }

    public IncogitoEventsUri events() {
        return new IncogitoEventsUri(baseurl.clone().path("events"));
    }

    public String personImage(String personId) {
        return rest.clone().segment("people", personId, "photo").build().toString();
    }

    public static class IncogitoEventsUri {
        private final UriBuilder events;

        public IncogitoEventsUri(UriBuilder events) {
            this.events = events;
        }

        public IncogitoEventUri eventUri(String name) {
            return new IncogitoEventUri(events.clone().segment(name));
        }

        public static class IncogitoEventUri {
            private final UriBuilder event;

            public IncogitoEventUri(UriBuilder event) {
                this.event = event;
            }

            public String calendarHtml() {
                return event.clone().segment("calendar").build().toString();
            }

            public String sessionListHtml() {
                return event.clone().segment("sessions").build().toString();
            }

            public String toString() {
                return event.build().toString();
            }
        }
    }

    public static class IncogitoRestEventsUri {
        public final IncogitoUri incogitoUri;
        private final UriBuilder events;

        public IncogitoRestEventsUri(IncogitoUri incogitoUri, UriBuilder events) {
            this.incogitoUri = incogitoUri;
            this.events = events;
        }

        public String toString() {
            return events.build().toString();
        }

        public IncogitoRestEventUri eventUri(String event) {
            return new IncogitoRestEventUri(this, events.clone().segment(event));
        }

        public static class IncogitoRestEventUri {
            public final IncogitoRestEventsUri restEventsUri;
            private final UriBuilder event;
            private final UriBuilder sessions;

            private IncogitoRestEventUri(IncogitoRestEventsUri restEventsUri, UriBuilder event) {
                this.restEventsUri = restEventsUri;
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
