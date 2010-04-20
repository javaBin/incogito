package no.java.incogito.ems.client;

import static java.net.URI.*;
import no.java.ems.external.v2.*;
import org.apache.commons.httpclient.*;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.client.*;
import org.slf4j.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class RESTfulEmsV2ClientFactoryBean implements FactoryBean {

    private static final Logger log = LoggerFactory.getLogger(RESTfulEmsV2ClientFactoryBean.class);

    private String baseurl;

    private String username;

    private String password;

    private HTTPCache cache;

    @Required
    public void setBaseurl(String baseurl) {
        this.baseurl = baseurl;
    }

    public void setCache(HTTPCache cache) {
        this.cache = cache;
    }

    public Object getObject() throws Exception {
        log.info("Creating EMS client. Cache={}, URL={}", cache, baseurl);

        if (cache == null) {
            cache = new HTTPCache(
                new MemoryCacheStorage(),
                new HTTPClientResponseResolver(new HttpClient(new MultiThreadedHttpConnectionManager())));
        }

        RESTfulEmsV2Client client = new RESTfulEmsV2Client(cache, username, password);
        client.login(create(baseurl));
        return client;
    }

    public Class getObjectType() {
        return RESTfulEmsV2Client.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
