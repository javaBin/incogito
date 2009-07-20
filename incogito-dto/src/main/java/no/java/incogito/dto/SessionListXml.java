package no.java.incogito.dto;

import fj.F;
import fj.data.Java;
import fj.data.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@XmlRootElement
public class SessionListXml {
    private java.util.List<SessionXml> sessions;

    public SessionListXml() {
    }

    public SessionListXml(java.util.List<SessionXml> sessions) {
        this.sessions = sessions;
    }

    public static final F<List<SessionXml>, SessionListXml> sessionListXml = new F<List<SessionXml>, SessionListXml>() {
        public SessionListXml f(List<SessionXml> sessionXmlList) {
            return new SessionListXml(Java.<SessionXml>List_ArrayList().f(sessionXmlList));
        }
    };

    public java.util.List<SessionXml> getSessions() {
        return sessions;
    }

    public void setSessions(java.util.List<SessionXml> sessions) {
        this.sessions = sessions;
    }
}
