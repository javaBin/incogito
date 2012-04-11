package no.java.incogito.web.servlet;

import no.arktekk.cms.CmsClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Thor Åge Eldby (thoraageeldby@gmail.com)
 */
public class CmsClientFilter implements Filter {

    private CmsClient cmsClient;

    @Autowired
    public CmsClientFilter(CmsClient cmsClient) {
        this.cmsClient = cmsClient;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(final ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String pathInfo = ((HttpServletRequest) request).getRequestURI();
        pathInfo = pathInfo.replaceAll("^.*\\/", "").replaceAll("\\.jspx", "").toLowerCase();
        request.setAttribute("cms", new CmsProperties(cmsClient, pathInfo));
        request.setAttribute("ball", "shit");
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

}