package no.java.incogito.application;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import voldemort.client.SocketStoreClientFactory;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SocketStoreClientFactoryFactoryBean implements FactoryBean, DisposableBean {
    private SocketStoreClientFactory clientFactory;

    private String url;

    @Required
    public void setUrl(String url) {
        this.url = url;
    }

    // -----------------------------------------------------------------------
    // FactoryBean Implementation
    // -----------------------------------------------------------------------

    public synchronized Object getObject() throws Exception {
        if (clientFactory == null) {
            clientFactory = new SocketStoreClientFactory(url);
        }
        return clientFactory;
    }

    public Class getObjectType() {
        return SocketStoreClientFactory.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        if (clientFactory != null) {
            clientFactory.close();
        }
    }
}
