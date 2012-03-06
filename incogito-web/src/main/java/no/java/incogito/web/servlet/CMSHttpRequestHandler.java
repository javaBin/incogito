package no.java.incogito.web.servlet;

import no.arktekk.cms.CmsClient;
import no.arktekk.cms.CmsEntry;
import no.arktekk.cms.CmsSlug;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import scala.Option;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
@Component("cmsHttpRequestHandler")
public class CmsHttpRequestHandler implements HttpRequestHandler {

    private CmsClient cmsClient;

    public CmsHttpRequestHandler() {
        cmsClient = new CmsClientFactory(url("http://wiki.java.no/poop"), url("http://wiki.java.no/rest/atompub/latest/spaces/javazone2012/pages/12682119/children")).build();
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        pathInfo = pathInfo.replaceAll("^.*\\/", "");
        System.out.println("Path: " + pathInfo);
        Option<CmsEntry> cmsEntryOption = cmsClient.fetchPageBySlug(CmsSlug.fromString(pathInfo));
        System.out.println("Entry: " + cmsEntryOption);
    }

    private static URL url(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
