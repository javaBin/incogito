package no.java.incogito.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class SessionXml {
    private String selfUri;

    private String title;

    public SessionXml() {
    }

    public SessionXml(URI selfUri, String title) {
        this.selfUri = selfUri.toString();
        this.title = title;
    }

    public String getSelfUri() {
        return selfUri;
    }

    public void setSelfUri(String selfUri) {
        this.selfUri = selfUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
