package no.java.incogito.ems.client;

import no.java.ems.client.RestEmsService;
import no.java.ems.service.EmsService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RestEmsServiceFactoryBean implements FactoryBean {
    private String baseurl;

    private boolean cache;

    @Required
    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public Object getObject() throws Exception {
        return new RestEmsService(baseurl, cache);
    }

    public Class getObjectType() {
        return EmsService.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
