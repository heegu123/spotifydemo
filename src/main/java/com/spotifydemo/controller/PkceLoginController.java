package com.spotifydemo.controller;

import com.spotifydemo.factory.SpotifyApiFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class PkceLoginController {

    private final SpotifyApiFactory spotifyApiFactory;

    // code_verifier, code_challenge 생성
    @GetMapping("/pkce")
    public PkceResponse generatePkce() {

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);

        return new PkceResponse(codeVerifier, codeChallenge);
    }

    // 프론트한테 code 받아서 access token 발급
    @PostMapping("/callback")
    public String getToken(@RequestParam String code,
                           @RequestParam String codeVerifier) {

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();

        try {
            AuthorizationCodePKCERequest request =
                    spotifyApi.authorizationCodePKCE(code, codeVerifier).build();

            AuthorizationCodeCredentials credentials = request.execute();

            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            return "success! accessToken = " + credentials.getAccessToken();

        } catch (Exception e) {
            return "login failed: " + e.getMessage();
        }
    }

    // ------------------------
    // 유틸 메서드 (PKCE 생성)
    // ------------------------

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] code = new byte[32];
        secureRandom.nextBytes(code);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(code);
    }

    private String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }

    // ------------------------
    // 응답 DTO
    // ------------------------

    record PkceResponse(String codeVerifier, String codeChallenge) {}
}

