package com.spotifydemo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "USER_DETAILS")
@Getter
@Setter
public class SpotifyUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @Column(name = "ACCESS_TOKEN", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "REFRESH_TOKEN", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "REF_ID")
    private String refId;

    // More information as per your need

}
