package no.java.incogito.web.servlet;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoBean {
    public final String baseurl;

    public IncogitoBean(String baseurl) {
        this.baseurl = baseurl;
    }

    public String getBaseurl() {
        return baseurl;
    }
}
