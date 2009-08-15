package no.java.incogito.domain;

import fj.F;
import fj.F3;
import static fj.Function.curry;
import fj.control.parallel.Callables;
import fj.data.Option;
import fj.pre.Ord;
import fj.pre.Show;
import no.java.incogito.Enums;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Level {
    public final LevelId id;

    public final String displayName;

    public final File iconFile;

    public static F<Level, File> iconFile_ = new F<Level, File>() {
        public File f(Level level) {
            return level.iconFile;
        }
    };
    public static Show<Level> showId = Show.showS(new F<Level, String>() {
        public String f(Level level) {
            return level.id.name();
        }
    });
    public static F<LevelId, F<String, F<File, Level>>> level_ = curry(new F3<LevelId, String, File, Level>() {
        public Level f(LevelId levelId, String displayName, File file) {
            return new Level(levelId, displayName, file);
        }
    });

    public Level(LevelId id, String displayName, File iconFile) {
        this.id = id;
        this.displayName = displayName;
        this.iconFile = iconFile;
    }

    public enum LevelId {
        Introductory,
        Introductory_Intermediate,
        Intermediate,
        Intermediate_Advanced,
        Advanced;

        public static Ord<LevelId> ord = Enums.ord();

        public static F<String, Option<LevelId>> valueOf_ = Enums.<LevelId>valueOf().f(LevelId.class);

        public static final Show<LevelId> show = fj.pre.Show.anyShow();

        public static final F<String, Option<LevelId>> valueOf = new F<String, Option<LevelId>>() {
            public Option<LevelId> f(final String value) {
                return Callables.<LevelId>either(new Callable<LevelId>() {
                    public LevelId call() throws Exception {
                        return valueOf(value);
                    }
                })._1().right().toOption();
            }
        };
    }
}
