package com.vert.catalogo.config.filters;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.vert.catalogo.services.impl.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String jwtToken = null;
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        // 1. Intentar obtener de la cabecera Authorization
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwtToken = authHeader.replace("Bearer ", "");
        }
        // 2. Si no viene en la cabecera, extraer de la cookie 'access_token'
        else if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }
        // 3. Validar y autenticar en SecurityContext
        if (jwtToken != null) {
            DecodedJWT decodedJWT = jwtService.validarToken(jwtToken);
            String username = jwtService.obtenerUsernameDeToken(decodedJWT);
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, null);
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        filterChain.doFilter(request, response);
    }
}
