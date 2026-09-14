package com.vert.catalogo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.vert.catalogo.config.filters.JwtFilter;
import com.vert.catalogo.services.impl.JwtService;

@Configuration
@PropertySource("classpath:jwt.properties")
@EnableWebSecurity
public class SecurityConfig {

    private JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoint público de autenticación
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/**").permitAll()

                        // 2. Endpoints públicos de listado y consulta (solo lectura / GET)
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/subcategorias", "/api/subcategorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()

                        // 3. Endpoints de gestión (Creaciones, modificaciones y borrado requieren autenticación)
                        .requestMatchers(HttpMethod.POST, "/api/categorias", "/api/categorias/**",
                                                           "/api/subcategorias", "/api/subcategorias/**",
                                                           "/api/productos", "/api/productos/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**",
                                                         "/api/subcategorias/**",
                                                         "/api/productos/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/categorias/**",
                                                           "/api/subcategorias/**",
                                                           "/api/productos/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**",
                                                            "/api/subcategorias/**",
                                                            "/api/productos/**").authenticated()

                        // 4. Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .addFilterBefore(new JwtFilter(jwtService), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationProvider authProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    AuthenticationManager authManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
