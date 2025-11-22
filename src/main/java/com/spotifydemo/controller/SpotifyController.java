package com.spotifydemo.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.spotifydemo.controller.dto.TrackDto;
import com.spotifydemo.factory.SpotifyApiFactory;
import com.spotifydemo.entity.SpotifyUser;
import com.spotifydemo.entity.SpotifyUserRepository;
import com.spotifydemo.service.UserProfileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.SavedAlbum;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import se.michaelthelin.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SpotifyController {

    private final SpotifyApiFactory spotifyApiFactory;
    private final UserProfileService userProfileService;
    private final SpotifyUserRepository spotifyUserRepository;

    @GetMapping("code")
    public String getCode() {
        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();

        List<String> scopes = new ArrayList<>();
        scopes.add("user-library-read");
        scopes.add("user-top-read");
        scopes.add("user-read-email");
        scopes.add("user-top-read");
        scopes.add("user-modify-playback-state");
        scopes.add("user-read-playback-state");

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope(String.join(" ", scopes))
                .show_dialog(true)
                .build();

        final URI uri = authorizationCodeUriRequest.execute();
        return uri.toString();
    }

    @GetMapping(value = "get-user-code/")
    public String getSpotifyUserCode(@RequestParam("code") String userCode, HttpServletResponse response)	throws IOException {
        return userCode;
    }

    @GetMapping(value = "/accesstoken")
    public String getAccessToken(@RequestParam("code") String userCode) {

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();

        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(userCode).build();
        try {
            final AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            String accessToken = credentials.getAccessToken();
            String refreshToken = credentials.getRefreshToken();

            final GetCurrentUsersProfileRequest getCurrentUsersProfile = spotifyApi.getCurrentUsersProfile().build();

            return "accesstoken: " + accessToken + "\nrefreshtoken: " + refreshToken;

        } catch (Exception e) {
            System.out.println("Exception occured while getting user code: " + e);
            return null;
        }
    }
}