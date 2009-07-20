package no.java.incogito.web.resources;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import no.java.incogito.application.OperationResult;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoRequestFilter implements ContainerResponseFilter {
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        System.out.println("response.getEntity() = " + response.getEntity());
        System.out.println("response.getEntityType() = " + response.getEntityType());
        if(response.getEntityType() == null || !OperationResult.class.isAssignableFrom((Class)response.getEntityType())) {
            return response;
        }

        OperationResult result = (OperationResult) response.getEntity();

        if(result.hasValue()) {
            response.setEntity(result.value());
        }
        else {
            response.setEntity(null);
        }

        return response;
    }
}
