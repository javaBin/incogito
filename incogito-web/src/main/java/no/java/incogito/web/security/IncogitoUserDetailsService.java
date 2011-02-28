package no.java.incogito.web.security;

import static java.util.Collections.singletonList;
import no.java.incogito.application.*;
import static no.java.incogito.domain.User.UserId.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.dao.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;

import java.util.*;

@Component("incogito-user-details-service")
public class IncogitoUserDetailsService implements UserDetailsService {

    private final Log log = LogFactory.getLog(getClass());

    private final IncogitoApplication application;

    @Autowired
    public IncogitoUserDetailsService(IncogitoApplication application) {
        this.application = application;
    }

    // -----------------------------------------------------------------------
    // UserDetailsService Implementation
    // -----------------------------------------------------------------------

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        System.out.println("IncogitoUserDetailsService.loadUserByUsername: username = " + username);

        if (StringUtils.isEmpty(username)) {
            throw new UsernameNotFoundException("username is empty");
        }

        OperationResult<no.java.incogito.domain.User> operationResult = application.getUser(userIdFromString(username));

        if (operationResult.isOk()) {
            return new IncogitoUserDetails(operationResult.value());
        }

        throw new UsernameNotFoundException("Hm");
    }

    public static class IncogitoUserDetails implements UserDetails {
        private static final List<GrantedAuthority> authorities = singletonList((GrantedAuthority) new GrantedAuthorityImpl("ROLE_USER"));

        public final no.java.incogito.domain.User user;

        public IncogitoUserDetails(no.java.incogito.domain.User user) {
            this.user = user;
        }

        // -----------------------------------------------------------------------
        // UserDetails Implementation
        // -----------------------------------------------------------------------

        public Collection<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        public String getPassword() {
            throw new RuntimeException("Not implemented");
        }

        public String getUsername() {
            return user.id.value;
        }

        public boolean isAccountNonExpired() {
            return true;
        }

        public boolean isAccountNonLocked() {
            return true;
        }

        public boolean isCredentialsNonExpired() {
            return true;
        }

        public boolean isEnabled() {
            return true;
        }
    }
}
