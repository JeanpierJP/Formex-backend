package com.superinka.formex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${auth0.audience}")
        private String audience;

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private String issuer;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                        .csrf(csrf -> csrf.disable())
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                .requestMatchers("/api/health").permitAll()

                                // Rutas públicas y de error
                                .requestMatchers("/api/public/**", "/auth/**", "/error").permitAll()
                                .requestMatchers("/api/payments/stripe/webhook").permitAll()
                                .requestMatchers("/images/**", "/uploads/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/support").permitAll()

                                // Gestión de cursos
                                .requestMatchers(HttpMethod.POST, "/api/courses").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.DELETE, "/api/courses/**")
                                .hasAnyRole("ADMIN", "INSTRUCTOR")

                                .anyRequest().authenticated()
                        )
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt
                                        .decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                )
                        );

                return http.build();
        }

        @Bean
        JwtDecoder jwtDecoder() {
                NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                        JwtDecoders.fromOidcIssuerLocation(issuer);

                OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
                OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
                OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

                jwtDecoder.setJwtValidator(withAudience);

                return jwtDecoder;
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
                converter.setAuthoritiesClaimName("https://formex.com/roles");
                converter.setAuthorityPrefix("");

                JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
                jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
                return jwtConverter;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of(
                        "http://localhost:5173",
                        "https://www.formex.digital"
                ));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
                private final String audience;

                AudienceValidator(String audience) {
                        this.audience = audience;
                }

                public OAuth2TokenValidatorResult validate(Jwt jwt) {
                        if (jwt.getAudience().contains(audience)) {
                                return OAuth2TokenValidatorResult.success();
                        }
                        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "The required audience is missing", null));
                }
        }
}
