package no.java.incogito.application;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import voldemort.client.SocketStoreClientFactory;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SocketStoreClientFactoryBean implements FactoryBean {

    private SocketStoreClientFactory socketStoreClientFactory;

    private String name;

    @Required
    public void setSocketStoreClientFactory(SocketStoreClientFactory socketStoreClientFactory) {
        this.socketStoreClientFactory = socketStoreClientFactory;
    }

    @Required
    public void setName(String name) {
        this.name = name;
    }

    // -----------------------------------------------------------------------
    // FactoryBean Implementation
    // -----------------------------------------------------------------------

    public Object getObject() throws Exception {
        return socketStoreClientFactory.getStoreClient(name);
    }

    public Class getObjectType() {
        return voldemort.client.StoreClient.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
