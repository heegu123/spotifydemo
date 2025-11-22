package com.spotifydemo.service;

import com.spotifydemo.entity.SpotifyUser;
import com.spotifydemo.entity.SpotifyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.Objects;

@Service
public class UserProfileService {

    @Autowired
    private SpotifyUserRepository spotifyUserRepository;

    @Transactional
    public SpotifyUser insertOrUpdateSpotifyUser(User user, String accessToken, String refreshToken) {
        SpotifyUser spotifyUser = spotifyUserRepository.findByRefId(user.getId());

        if (Objects.isNull(spotifyUser)) {
            spotifyUser = new SpotifyUser();
        }

        spotifyUser.setUserName(user.getDisplayName());
        spotifyUser.setEmailId(user.getEmail());
        spotifyUser.setAccessToken(accessToken);
        spotifyUser.setRefreshToken(refreshToken);
        spotifyUser.setRefId(user.getId());
        return spotifyUserRepository.save(spotifyUser);
    }
}
