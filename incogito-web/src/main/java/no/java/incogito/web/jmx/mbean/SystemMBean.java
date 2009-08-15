package no.java.incogito.web.jmx.mbean;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import static java.net.InetAddress.getLocalHost;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@ManagedResource("incogito:name=System")
@Component
public class SystemMBean {

    private final String hostname;

    public SystemMBean() throws UnknownHostException {
        hostname = getLocalHost().getHostName();
    }

    @ManagedAttribute(description = "yo")
    public String getHostname() {
        return hostname;
    }
}
