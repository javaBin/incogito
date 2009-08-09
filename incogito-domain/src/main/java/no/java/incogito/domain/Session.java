package no.java.incogito.domain;

import fj.F;
import fj.pre.Show;
import static fj.pre.Show.anyShow;
import fj.control.parallel.Callables;
import fj.data.List;
import fj.data.Option;
import org.joda.time.Interval;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Session {
    public enum Level {
        Introductory,
        Introductory_Intermediate,
        Intermediate,
        Intermediate_Advanced,
        Advanced;

        public static final Show<Level> show = anyShow();

        public static final F<String, Option<Level>> valueOf = new F<String, Option<Level>>() {
            public Option<Level> f(final String value) {
                return Callables.<Level>either(new Callable<Level>() {
                    public Level call() throws Exception {
                        return valueOf(value);
                    }
                })._1().right().toOption();
            }
        };
    }

    public final SessionId id;
    public final String title;
    public final Option<WikiString> _abstract;
    public final Option<WikiString> body;
    public final Option<Level> level;
    public final Option<Interval> timeslot;
    public final Option<String> room;
    public final List<String> labels;
    public final List<Speaker> speakers;
    public final List<Comment> comments;

    public Session(SessionId id, String title, Option<WikiString> _abstract, Option<WikiString> body,
                   Option<Level> level, Option<Interval> timeslot, Option<String> room, List<String> labels,
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
}
