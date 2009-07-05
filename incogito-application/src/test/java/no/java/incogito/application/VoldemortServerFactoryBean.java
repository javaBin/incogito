package no.java.incogito.application;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VoldemortServerFactoryBean implements FactoryBean, DisposableBean {
    VoldemortServer voldemortServer;

    public Object getObject() throws Exception {
        // TODO: This should be configurable - Trygve
        File clusterXml = new File(getClass().getResource("/cluster-it/node-it/config/cluster.xml").toURI().getPath());
        String voldemortHome = clusterXml.getParentFile().getParentFile().getAbsolutePath();

        VoldemortConfig config = VoldemortConfig.loadFromVoldemortHome(voldemortHome);
        voldemortServer = new VoldemortServer(config);
        voldemortServer.start();

        return voldemortServer;
    }

    public Class getObjectType() {
        return VoldemortServer.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        if (voldemortServer != null) {
            voldemortServer.stop();
        }
    }
}
