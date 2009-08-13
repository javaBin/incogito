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
    public String baseurl;
    public String currentUser;

    public IncogitoXml() {
    }

    public IncogitoXml(String baseurl, Option<String> currentUser) {
        this.baseurl = baseurl;
        this.currentUser = currentUser.orSome((String) null);
    }
}
