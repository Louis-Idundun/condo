package com.condo.condo.configuration;


import com.condo.condo.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityFilterConfig {
    private final JwtAuthenticationFilter jwtFilterConfiguration;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutConfig logoutConfiguration;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/v1/admins/**").hasAnyAuthority(Role.ADMIN.name())
                        .requestMatchers("/api/v1/users/**").hasAnyAuthority(Role.ADMIN.name(), Role.CUSTOMER.name(), Role.OWNER.name())
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-ui/**",
                                "configuration/security",
                                "configuration/ui",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilterConfiguration, UsernamePasswordAuthenticationFilter.class)
                .logout(httpSecurityLogoutConfigurer -> {
                    httpSecurityLogoutConfigurer
                            .logoutUrl("/auth/logout")
                            .addLogoutHandler(logoutConfiguration)
                            .logoutSuccessHandler((request, response, authenticate) ->
                                    SecurityContextHolder.clearContext()
                            );
                });
        return httpSecurity.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name()
        ));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of(
                "X-CSRF-TOKEN",
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.ACCEPT,
                HttpHeaders.CONTENT_TYPE
        ));
        configuration.setAllowedOriginPatterns(List.of(
                "/**"
        ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

//public class SecurityConfiguration {
//
//    private final JwtAuthenticationFilter jwtAuthFilter;
//    private final AuthenticationProvider authenticationProvider;
//    private final LogoutConfig logoutConfig;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
//        CookieClearingLogoutHandler cookies = new CookieClearingLogoutHandler("our-custom-cookie");
//        httpSecurity
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(requests -> requests
//                        .requestMatchers("/auth/**",
//                                "/swagger-ui.html",
//                                "/swagger-ui/**",
//                                "/v3/api-docs/**",
//                                "/health")
//                        .permitAll() // Public endpoints
//                        .requestMatchers(HttpMethod.GET, "/api/properties/**").hasAnyRole("CUSTOMER", "OWNER", "ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/properties/**").hasRole("OWNER")
//                        .requestMatchers(HttpMethod.PUT, "/api/properties/**").hasRole("OWNER")
//                        .requestMatchers(HttpMethod.DELETE, "/api/properties/**").hasRole("OWNER")
//                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Admin-specific endpoints
//                        .requestMatchers("/api/bookings/**").hasAnyRole("CUSTOMER", "ADMIN") // Booking endpoints for Customers and Admins
//                        .anyRequest().authenticated() // Any other requests must be authenticated
//                )
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .sessionManagement(request -> request.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authenticationProvider(authenticationProvider)
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .logout(logoutConfigurer -> logoutConfigurer
//                        .logoutUrl("/auth/logout")
//                        .addLogoutHandler(logoutConfig)
//                        .addLogoutHandler(cookies)
//                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
//                );
//
//        return httpSecurity.build();
//    }
//
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of(
//                "http://localhost:3000"
// //               "https://condo.com"
//        ));
//        configuration.setAllowedMethods(Arrays.asList(
//                HttpMethod.GET.name(),
//                HttpMethod.POST.name(),
//                HttpMethod.PATCH.name(),
//                HttpMethod.DELETE.name(),
//                HttpMethod.PUT.name()
//        ));
//        configuration.setAllowCredentials(true);
//        configuration.setAllowedHeaders(List.of(
//                "X-CSRF-TOKEN",
//                HttpHeaders.AUTHORIZATION,
//                HttpHeaders.ACCEPT,
//                HttpHeaders.CONTENT_TYPE
//        ));
//        configuration.setAllowedOriginPatterns(List.of(
//                "/**"
//        ));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}
