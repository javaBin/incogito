package no.java.incogito.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ResourceToFileFactoryBean implements FactoryBean {

    private static final Logger log = LoggerFactory.getLogger(ResourceToFileFactoryBean.class);

    private String resource;

    @Required
    public void setResource(String resource) {
        this.resource = resource;
    }

    public Object getObject() throws Exception {

        URL resource = this.getClass().getClassLoader().getResource(this.resource);

        if (resource == null) {
            throw new Exception("Could not find resource: " + resource);
        }

        File file = new File(resource.toURI().getPath()).getAbsoluteFile();

        log.debug("Resolved '" + resource + "' to: " + file);

        return new File(file, this.resource);
    }

    public Class getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
