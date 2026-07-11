package com.helpdesk.config;

import com.helpdesk.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            // ✅ Enable CORS so preflight requests get proper headers
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ✅ Disable Spring's built-in form login entirely
            .formLogin(form -> form.disable())

            // ✅ Disable Spring's built-in /logout redirect
            .logout(logout -> logout.disable())

            // ✅ Disable HTTP Basic auth popup
            .httpBasic(basic -> basic.disable())

            // ✅ Stateless — no sessions
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ Return 401 JSON instead of redirecting to /login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\"}");
                })
            )

            .authorizeHttpRequests(auth -> auth
                // ✅ Let CORS preflight requests through unauthenticated
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // All static files and auth endpoints are public
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/dashboard.html",
                    "/tickets.html",
                    "/new-ticket.html",
                    "/ticket-detail.html",
                    "/admin.html",
                    "/css/**",
                    "/js/**",
                    "/api/auth/**"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OncePerRequestFilter jwtAuthFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain)
                    throws ServletException, IOException {

                String header = req.getHeader("Authorization");
                if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                    String token = header.substring(7);
                    if (jwtUtils.validateToken(token)) {
                        String username = jwtUtils.getUsernameFromToken(token);
                        var ud = userDetailsService.loadUserByUsername(username);
                        var auth = new org.springframework.security.authentication
                                .UsernamePasswordAuthenticationToken(
                                    ud, null, ud.getAuthorities());
                        org.springframework.security.core.context.SecurityContextHolder
                                .getContext().setAuthentication(auth);
                    }
                }
                chain.doFilter(req, res);
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}