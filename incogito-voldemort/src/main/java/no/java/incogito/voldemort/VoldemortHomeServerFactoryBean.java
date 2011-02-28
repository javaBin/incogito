package no.java.incogito.voldemort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import voldemort.*;
import voldemort.server.VoldemortConfig;
import voldemort.server.VoldemortServer;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VoldemortHomeServerFactoryBean implements FactoryBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(VoldemortHomeServerFactoryBean.class);

    private File voldemortHome;

    private VoldemortServer voldemortServer;

    @Required
    public void setVoldemortHome(File voldemortHome) {
        this.voldemortHome = voldemortHome;
    }

    public Object getObject() throws Exception {
        log.info("Starting Voldemort. Home directory: " + voldemortHome.getAbsolutePath());

//        File clusterXml = new File(getClass().getResource("/cluster-it/node-it/config/cluster.xml").toURI().getPath());
//        String voldemortHome = clusterXml.getParentFile().getParentFile().getAbsolutePath();

        VoldemortConfig config = VoldemortConfig.loadFromVoldemortHome(voldemortHome.getAbsolutePath());
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
            try {
                voldemortServer.stop();
            } catch (VoldemortException e) {
                // ignore, voldemort always throws an exception
            }
        }
    }
}
