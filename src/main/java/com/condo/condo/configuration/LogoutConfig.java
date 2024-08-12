package com.condo.condo.configuration;

import com.condo.condo.exceptions.FlexisafException;
import com.condo.condo.services.implementations.JwtImplementation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class LogoutConfig implements LogoutHandler {
    private final UserDetailsService userDetailsService;
    private final JwtImplementation jwtImplementation;
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer")){
            return;
        }

        String jwtToken = header.substring(7);

        if(jwtImplementation.isExpired(jwtToken)) {
            throw new FlexisafException("Your session has expired. Please login");
        }

        String email = jwtImplementation.extractEmailAddressFromToken(jwtToken);
        if (email != null) {
            new UsernamePasswordAuthenticationToken(
                    this.userDetailsService.loadUserByUsername(email),
                    null, Collections.emptyList()
            );
        }
    }
}
