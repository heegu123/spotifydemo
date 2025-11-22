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
import se.michaelthelin.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import se.michaelthelin.spotify.requests.data.player.GetUsersAvailableDevicesRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SpotifyController {

    @Value("${custom.server.ip}")
    private String customIp;

    private final SpotifyApiFactory spotifyApiFactory;
    private final UserProfileService userProfileService;
    private final SpotifyUserRepository spotifyUserRepository;

    @GetMapping("login")
    public String spotifyLogin() {
        System.out.println("1. /login - Login START!");
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
        System.out.println("2. /login - LOGIN FINISHED");
        System.out.println("3. /login - uri: " + uri);
        return uri.toString();
    }

    @GetMapping(value = "get-user-code/")
    public void getSpotifyUserCode(@RequestParam("code") String userCode, HttpServletResponse response)	throws IOException {
        System.out.println("1. /get-user-code/ - GET_USER_CODE START!");
        System.out.println("2. /get-user-code/ - userCode= "+ userCode);
        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();

        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(userCode).build();
        User user = null;

        try {
            final AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            final GetCurrentUsersProfileRequest getCurrentUsersProfile = spotifyApi.getCurrentUsersProfile().build();
            user = getCurrentUsersProfile.execute();

            userProfileService.insertOrUpdateSpotifyUser(user, credentials.getAccessToken(), credentials.getRefreshToken());
        } catch (Exception e) {
            System.out.println("Exception occured while getting user code: " + e);
        }
        System.out.println("3. /get-user-code/ - userId= " + user.getId());
        response.sendRedirect(customIp + "/home?id="+user.getId());
    }

    @GetMapping(value = "user-saved-album")
    public SavedAlbum[] getCurrentUserSavedAlbum(@RequestParam String userId) {
        SpotifyUser spotifyUser = spotifyUserRepository.findByRefId(userId);

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();
        spotifyApi.setAccessToken(spotifyUser.getAccessToken());
        spotifyApi.setRefreshToken(spotifyUser.getRefreshToken());
        System.out.println(spotifyUser.getAccessToken());
        final GetCurrentUsersSavedAlbumsRequest getUsersTopArtistsRequest = spotifyApi.getCurrentUsersSavedAlbums()
                .limit(50)
                .offset(0)
                .build();

        try {
            final Paging<SavedAlbum> artistPaging = getUsersTopArtistsRequest.execute();

            return artistPaging.getItems();
        } catch (Exception e) {
            System.out.println("Exception occured while fetching user saved album: " + e);
        }

        return new SavedAlbum[0];
    }

    @GetMapping(value = "user-top-songs")
    public Track[] getUserTopTracks(@RequestParam String userId) {
        SpotifyUser spotifyUser = spotifyUserRepository.findByRefId(userId);

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();
        spotifyApi.setAccessToken(spotifyUser.getAccessToken());
        spotifyApi.setRefreshToken(spotifyUser.getRefreshToken());

        final GetUsersTopTracksRequest getUsersTopTracksRequest = spotifyApi.getUsersTopTracks()
                .time_range("medium_term")
                .limit(10)
                .offset(0)
                .build();

        try {
            final Paging<Track> trackPaging = getUsersTopTracksRequest.execute();

            return trackPaging.getItems();
        } catch (Exception e) {
            System.out.println("Exception occured while fetching top songs: " + e);
        }

        return new Track[0];
    }

    @GetMapping("track")
    public List<TrackDto> searchTrack(@RequestParam String q, @RequestParam String userId){
        SpotifyUser spotifyUser = spotifyUserRepository.findByRefId(userId);

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();
        spotifyApi.setAccessToken(spotifyUser.getAccessToken());
        spotifyApi.setRefreshToken(spotifyUser.getRefreshToken());

        final SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(q)
                .limit(10)
                .offset(0)
                .build();

        try {
            Paging<Track> trackPaging = searchTracksRequest.execute();

            return Arrays.stream(trackPaging.getItems())
                    .map(TrackDto::from)
                    .collect(Collectors.toList());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    @PostMapping("/playback")
    public void resumePlayback(@RequestParam String userId, String uri) {
        SpotifyUser spotifyUser = spotifyUserRepository.findByRefId(userId);

        SpotifyApi spotifyApi = spotifyApiFactory.createSpotifyApi();
        spotifyApi.setAccessToken(spotifyUser.getAccessToken());
        spotifyApi.setRefreshToken(spotifyUser.getRefreshToken());

        StartResumeUsersPlaybackRequest startResumeUsersPlaybackRequest = spotifyApi.startResumeUsersPlayback()
                .uris((JsonParser.parseString("[\"" + uri + "\"]").getAsJsonArray()))
                .build();

        try {
            startResumeUsersPlaybackRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}