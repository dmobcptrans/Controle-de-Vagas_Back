package com.cptrans.petrocarga.modules.googleAuth.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.shared.exceptions.GlobalHandlerExceptions;
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

        if (googleClientId == null || googleClientId.isEmpty()) throw new GlobalHandlerExceptions.GoogleIdNaoConfiguradoException();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;

        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            throw new AuthExceptions.GoogleTokenInvalidoException();
        }

        if (idToken == null) throw new AuthExceptions.GoogleTokenInvalidoException();

        Payload payload = idToken.getPayload();

        String issuer = payload.getIssuer();

        if (
            !issuer.equals("accounts.google.com") &&
            !issuer.equals("https://accounts.google.com")
        ) throw new AuthExceptions.GoogleTokenInvalidoException();

        if (payload.getEmailVerified() == null || !payload.getEmailVerified()) throw new AuthExceptions.GoogleTokenInvalidoException();
        
        return payload;
    }
}
