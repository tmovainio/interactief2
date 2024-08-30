package nl.utwente.di.interactief2.rest_api.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class Authenticator {

    private static final String KEY = "teamInteractief2_SecretSecurityKey";
    Algorithm algorithm;
    JWTVerifier verifier;

    public Authenticator() {
        algorithm = Algorithm.HMAC256(KEY);
        verifier = JWT.require(algorithm)
                .withIssuer(KEY)
                .build();
    }

    public String encodeJWToken(int studentNumber) {
        Instant now = Instant.now();
        String jwtToken = JWT.create()
                .withIssuer(KEY)
                .withSubject(KEY + " Details")
                .withClaim("studentNumber", studentNumber)
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(900))
                .withJWTId(UUID.randomUUID().toString())
                // .withNotBefore(now.plusMillis(325))
                .sign(algorithm);
        return jwtToken;
    }

    public int decodeJWToken(String jwtToken) throws JWTVerificationException {
        DecodedJWT decodedJWT;
        decodedJWT = verifier.verify(jwtToken);
        Claim claim = decodedJWT.getClaim("studentNumber");
        return claim.asInt();
    }
}
