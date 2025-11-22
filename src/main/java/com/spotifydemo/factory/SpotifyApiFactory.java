package com.spotifydemo.factory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;

import java.net.URI;

@Component
public class SpotifyApiFactory {

    @Value("${redirect.server.ip}")
    private String customIp;

    public SpotifyApi createSpotifyApi() {
        URI redirectedURL = SpotifyHttpManager.makeUri(customIp + "/api/get-user-code/");

        return new SpotifyApi.Builder()
                .setClientId("69c6deefe5fe49b194e8f88a2aaa5dd4")
                .setClientSecret("53f3ebd8e4a546eb88fa0d9eca5f93b9")
                .setRedirectUri(redirectedURL)
                .build();
    }
}