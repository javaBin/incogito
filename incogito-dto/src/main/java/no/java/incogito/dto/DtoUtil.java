package no.java.incogito.dto;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
class DtoUtil {
    public static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        for (T t : iterable) {
            list.add(t);
        }
        return list;
    }
}
