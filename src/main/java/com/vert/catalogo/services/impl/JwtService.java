package com.vert.catalogo.services.impl;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vert.catalogo.entities.Usuario;

@Service
public class JwtService {

    @Value("${jwt.private-key}")
    private String privateKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expiration-milis}")
    private long expirationMilis;

    public String crearToken(Authentication authentication) {
        Algorithm algorithm = Algorithm.HMAC256(privateKey);
        Usuario userDetails = (Usuario) authentication.getPrincipal();

        String token = JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuer(this.issuer)
                .withIssuedAt(new Date())
                .withExpiresAt(Instant.now().plusMillis(this.expirationMilis))
                .withNotBefore(Instant.now())
                .sign(algorithm);

        return token;
    }

    public DecodedJWT validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(privateKey);

            return JWT.require(algorithm)
                    .withIssuer(this.issuer)
                    .build()
                    .verify(token);

        } catch (JWTVerificationException jwtExc) {
            throw new JWTVerificationException("token invalido");
        }
    }

    public String obtenerUsernameDeToken(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public long getExpirationMilis() {
        return this.expirationMilis;
    }
}
