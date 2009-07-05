package no.java.incogito.ems.server;

import no.java.ems.server.EmsServices;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class EmsServicesFactoryBean implements FactoryBean, DisposableBean {

    private File emsHome;

    private int httpPort;

    private int derbyPort;

    private boolean startDatabase;

    private boolean dropTables;

    private boolean secure;

    private EmsServices emsServices;

    @Required
    public void setEmsHome(File emsHome) {
        this.emsHome = emsHome;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public void setDerbyPort(int derbyPort) {
        this.derbyPort = derbyPort;
    }

    public void setStartDatabase(boolean startDatabase) {
        this.startDatabase = startDatabase;
    }

    public void setDropTables(boolean dropTables) {
        this.dropTables = dropTables;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public Object getObject() throws Exception {
        return emsServices = new EmsServices(emsHome, httpPort, startDatabase, dropTables, derbyPort, secure);
    }

    public Class getObjectType() {
        return EmsServices.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        if (emsServices != null) {
            emsServices.stop();
        }
    }
}
