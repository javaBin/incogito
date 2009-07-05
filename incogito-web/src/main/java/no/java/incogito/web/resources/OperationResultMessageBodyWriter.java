package no.java.incogito.web.resources;

import no.java.incogito.application.OperationResult;
import no.java.incogito.dto.EventListXml;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class OperationResultMessageBodyWriter implements MessageBodyWriter<OperationResult> {

    private final JAXBContext context;

    OperationResultMessageBodyWriter() throws Exception {
        context = JAXBContext.newInstance(EventListXml.class);
    }

    @Produces("application/xml")
    @Consumes("application/xml")
    @Provider
    public static final class App extends OperationResultMessageBodyWriter {
        public App(@Context Providers ps) throws Exception {
        }
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return OperationResult.class.isAssignableFrom(type);
    }

    public long getSize(OperationResult operationResult, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public void writeTo(OperationResult operationResult, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            context.createMarshaller().marshal(operationResult.value(), entityStream);
        } catch (JAXBException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
