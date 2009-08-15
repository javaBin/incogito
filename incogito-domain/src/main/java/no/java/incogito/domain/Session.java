package no.java.incogito.domain;

import fj.data.List;
import fj.data.Option;
import org.joda.time.Interval;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Session {
    public final SessionId id;
    public final String title;
    public final Option<WikiString> _abstract;
    public final Option<WikiString> body;
    public final Option<Level> level;
    public final Option<Interval> timeslot;
    public final Option<String> room;
    public final List<Label> labels;
    public final List<Speaker> speakers;
    public final List<Comment> comments;

    public Session(SessionId id, String title, Option<WikiString> _abstract, Option<WikiString> body,
                   Option<Level> level, Option<Interval> timeslot, Option<String> room, List<Label> labels,
                   List<Speaker> speakers, List<Comment> comments ) {
        this.id = id;
        this.title = title;
        this._abstract = _abstract;
        this.body = body;
        this.level = level;
        this.timeslot = timeslot;
        this.room = room;
        this.labels = labels;
        this.speakers = speakers;
        this.comments = comments;
    }

    public Session timeslot(Option<Interval> timeslot) {
        return new Session(id, title, _abstract, body, level, timeslot, room, labels, speakers, comments);
    }

    public Session room(Option<String> room) {
        return new Session(id, title, _abstract, body, level, timeslot, room, labels, speakers, comments);
    }
}
