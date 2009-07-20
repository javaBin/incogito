package no.java.incogito.web.resources;

import com.sun.jersey.core.provider.jaxb.AbstractRootElementProvider;
import com.sun.jersey.core.impl.provider.entity.*;
import no.java.incogito.application.OperationResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class OperationResultMessageBodyWriter {

//    private final JAXBContext context;
//
//    OperationResultMessageBodyWriter() throws Exception {
//        context = JAXBContext.newInstance(EventListXml.class);
//    }

    @Produces({"application/xml", "application/json"})
    @Consumes({"application/xml", "application/json"})
//    @Provider
    public static final class App extends OperationResultMessageBodyWriter implements MessageBodyWriter<OperationResult> {
        private final JerseyHackAbstractRootElementProvider provider;

        public App(@Context Providers ps) throws Exception {
            provider = new JerseyHackAbstractRootElementProvider(ps);
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            System.out.println("OperationResultMessageBodyWriter$App.isWriteable, type = " + type + ", media type: " + mediaType);
            System.out.println("provider.isSupported(mediaType) = " + provider.isSupported(mediaType));
            System.out.println("provider.isSupported(mediaType) = " + provider.isSupported(MediaType.APPLICATION_SVG_XML_TYPE));
            return OperationResult.class.isAssignableFrom(type) && provider.isSupported(mediaType);
        }

        public long getSize(OperationResult operationResult, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(OperationResult operationResult, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            Object value = ((OperationResult) operationResult).value();
            type = value.getClass();
            genericType = value.getClass();

            System.out.println("value = " + value);
            System.out.println("value.getClass() = " + value.getClass());

            System.out.println("type = " + type);

            System.out.println("genericType = " + genericType);

            System.out.println("annotations.length = " + annotations.length);
            for (Annotation annotation : annotations) {
                System.out.println("annotation = " + annotation);
            }

            System.out.println("mediaType = " + mediaType);

            provider.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
        }
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private static class JerseyHackAbstractRootElementProvider extends AbstractRootElementProvider {
        public JerseyHackAbstractRootElementProvider(Providers ps) {
            super(ps, MediaType.APPLICATION_XML_TYPE);
        }

        public boolean isSupported(MediaType m) {
            return super.isSupported(m);
        }

        @Override
        protected void writeTo(Object t, MediaType mediaType, Charset c, Marshaller m, OutputStream entityStream) throws JAXBException, IOException {
            final Marshaller x = getMarshaller(t.getClass(), mediaType);
            System.out.println("x = " + x);
            System.out.println("x.getClass() = " + x.getClass());

            super.writeTo(t, mediaType, c, m, entityStream);
        }
    }
}
