package no.java.incogito.web.security;

import no.java.incogito.application.IncogitoApplication;
import no.java.incogito.application.OperationResult;
import no.java.incogito.domain.User;
import no.java.incogito.domain.User.UserId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ui.SpringSecurityFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class IncogitoSecurityFilter extends SpringSecurityFilter {

    private final Log log = LogFactory.getLog(IncogitoSecurityFilter.class);

    private static final String PROCESSED = "processed";

    private final IncogitoApplication application;

    @Autowired
    public IncogitoSecurityFilter(IncogitoApplication application) {
        this.application = application;
    }

    protected void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String remoteUser = request.getRemoteUser();
        if (remoteUser == null) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession();

        if (session.getAttribute(PROCESSED) != null) {
            chain.doFilter(request, response);
            return;
        }

        User user = User.createTransientUser(new UserId(remoteUser));
        OperationResult<User> operationResult = application.createUser(user);

        if (operationResult.isOk() || operationResult.isConflict()) {
            if (operationResult.isOk()) {
                log.warn("Creating new user: " + remoteUser);
            } else {
                log.info("User already exist: " + remoteUser);
            }

            session.setAttribute(PROCESSED, "yep");

            chain.doFilter(request, response);
            return;
        }

        throw new RuntimeException("Unable to ensure that the user exist.");
    }

    public int getOrder() {
        return LOWEST_PRECEDENCE; // Hm, is this right? should be overridden by <security:custom-filter position="LAST"/> I hope - trygve
    }
}
