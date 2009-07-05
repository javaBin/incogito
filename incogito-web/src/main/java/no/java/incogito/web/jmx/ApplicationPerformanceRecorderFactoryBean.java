package no.java.incogito.web.jmx;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ApplicationPerformanceRecorderFactoryBean implements FactoryBean {

    private final Class klass;

    public ApplicationPerformanceRecorderFactoryBean(Class klass) {
        this.klass = klass;
    }

    public Object getObject() throws Exception {

        Method[] methods = klass.getMethods();
        String[] names = new String[methods.length];
        for (int i = 0, methodsLength = methods.length; i < methodsLength; i++) {
            names[i] = methods[i].getName();
        }

        return new ApplicationPerformanceRecorder(names);
    }

    public Class getObjectType() {
        return ApplicationPerformanceRecorder.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
