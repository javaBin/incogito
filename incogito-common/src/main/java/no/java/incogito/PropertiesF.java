package no.java.incogito;

import fj.F;
import fj.P2;
import fj.Function;
import fj.F2;
import fj.Unit;
import fj.control.parallel.Callables;
import static fj.Function.compose;
import fj.pre.Ord;
import fj.data.TreeMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PropertiesF {

    public static final F<TreeMap<String, String>, Properties> propertiesFromMap = new F<TreeMap<String, String>, Properties>() {
        public Properties f(TreeMap<String, String> map) {
            Properties properties = new Properties();
            for (P2<String, String> p2 : map) {
                properties.setProperty(p2._1(), p2._2());
            }
            return properties;
        }
    };

    public static final F<Properties, TreeMap<String, String>> mapFromProperties = new F<Properties, TreeMap<String, String>>() {
        public TreeMap<String, String> f(Properties properties) {
            TreeMap<String, String> map = TreeMap.empty(Ord.stringOrd);

            for (Entry<Object, Object> entry : properties.entrySet()) {
                map = map.set(entry.getKey().toString(), entry.getValue().toString());
            }

            return map;
        }
    };

    public static final F<InputStream, Callable<Properties>> loadProperties = new F<InputStream, Callable<Properties>>() {
        public Callable<Properties> f(final InputStream inputStream) {
            return new Callable<Properties>() {
                public Properties call() throws Exception {
                    Properties properties = new Properties();
                    properties.load(inputStream);
                    return properties;
                }
            };
        }
    };

    public static final F<Properties, F<OutputStream, Callable<Unit>>> storeProperties = Function.curry(new F2<Properties, OutputStream, Callable<Unit>>() {
        public Callable<Unit> f(final Properties properties, final OutputStream outputStream) {
            return new Callable<Unit>() {
                public Unit call() throws Exception {
                    properties.store(outputStream, null);
                    return Unit.unit();
                }
            };
        }
    });

    public static final F<InputStream, Callable<TreeMap<String, String>>> loadPropertiesAsMap =
            compose(Callables.fmap(mapFromProperties), loadProperties);

    public static final F<TreeMap<String, String>, F<OutputStream, Callable<Unit>>> storePropertiesMap = compose(storeProperties, propertiesFromMap);
}
