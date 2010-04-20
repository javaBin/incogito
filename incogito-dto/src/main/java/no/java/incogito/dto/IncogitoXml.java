package no.java.incogito.dto;

import fj.data.Option;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygve.laugstol@arktekk.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement(name = "incogito")
@XmlAccessorType(XmlAccessType.FIELD)
public class IncogitoXml {
    public String eventsUrl;
    public String currentUser;

    public IncogitoXml() {
    }

    public IncogitoXml(String eventsUrl, Option<String> currentUser) {
        this.eventsUrl = eventsUrl;
        this.currentUser = currentUser.orSome((String) null);
    }
}
