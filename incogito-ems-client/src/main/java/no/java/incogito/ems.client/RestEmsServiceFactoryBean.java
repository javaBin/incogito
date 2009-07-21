package no.java.incogito.ems.client;

import no.java.ems.client.RestEmsService;
import no.java.ems.service.EmsService;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RestEmsServiceFactoryBean implements FactoryBean {

    private static final Log log = LogFactory.getLog(RestEmsServiceFactoryBean.class);

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
        log.info("Creating EMS client. Cache=" + cache + ", URL=" + baseurl);

        return new RestEmsService(baseurl, cache);
    }

    public Class getObjectType() {
        return EmsService.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
