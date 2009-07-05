package no.java.incogito.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class TestPathFactoryBean implements FactoryBean {

    private Class testClass;

    private String path;

    @Required
    public void setTestClass(Class testClass) {
        this.testClass = testClass;
    }

    @Required
    public void setPath(String path) {
        this.path = path;
    }

    public Object getObject() throws Exception {

        URL resource = testClass.getResource("/");

        if (resource == null) {
            throw new Exception("Could not find basedir");
        }

        File file = new File(resource.toURI().getPath()).getParentFile().getParentFile().getAbsoluteFile();

        File pomXml = new File(file, "pom.xml");

        if (!pomXml.canRead()) {
            throw new Exception("Unable to resolve path, got this far: " + file);
        }

        return new File(file, path);
    }

    public Class getObjectType() {
        return File.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
