package no.java.incogito.domain;

import fj.F;
import fj.F3;
import static fj.Function.curry;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Label {
    public final String id;

    public final String displayName;

    public final File iconFile;

    public static final F<Label, File> iconFile_ = new F<Label, File>() {
        public File f(Label label) {
            return label.iconFile;
        }
    };

    public Label(String id, String displayName, File iconFile) {
        this.id = id;
        this.displayName = displayName;
        this.iconFile = iconFile;
    }

    public static final F<String, F<String, F<File, Label>>> label_ = curry(new F3<String, String, File, Label>() {
        public Label f(String id, String displayName, File iconFile) {
            return new Label(id, displayName, iconFile);
        }
    });
}
