package com.cptrans.petrocarga.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;


@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    public Payload verifyGoogleToken(String idTokenString)  {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;

        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            throw new AuthorizationDeniedException("Token Google inválido");
        }

        if (idToken == null) {
           throw new AuthorizationDeniedException("Token Google inválido");
        }

        Payload payload = idToken.getPayload();

        String issuer = payload.getIssuer();

        if (!issuer.equals("accounts.google.com") &&
            !issuer.equals("https://accounts.google.com")) {
            throw new AuthorizationDeniedException("Token Google inválido");
        }

        if (payload.getEmailVerified() == null || !payload.getEmailVerified()) {
            throw new AuthorizationDeniedException("Token Google inválido");
        }

        return payload;
    }
}
