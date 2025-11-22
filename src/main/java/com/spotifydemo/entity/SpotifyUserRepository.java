package com.spotifydemo.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotifyUserRepository extends JpaRepository<SpotifyUser, Integer> {

    SpotifyUser findByRefId(String refId);
}
