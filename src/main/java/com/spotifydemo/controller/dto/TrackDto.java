package com.spotifydemo.controller.dto;

import lombok.Builder;
import lombok.Data;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@Builder
public class TrackDto {
    private String title;       // 곡 제목
    private String artistName;  // 아티스트명 (여러 명일 경우 콤마로 연결)
    private String albumName;   // 앨범명
    private String uri;         // 재생용 URI (spotify:track:...)
    private String spotifyUrl;  // 곡 링크 (클릭 시 스포티파이 웹으로 이동)
    private String imageUrl;    // 앨범 커버 이미지 (화면에 보여주기 위해 추가 추천)

    // ★ 핵심: Track 객체를 DTO로 변환하는 정적 팩토리 메서드
    public static TrackDto from(Track track) {
        return TrackDto.builder()
                .title(track.getName())
                .artistName(extractArtists(track))
                .albumName(track.getAlbum().getName())
                .uri(track.getUri())
                .spotifyUrl(track.getExternalUrls().get("spotify"))
                .imageUrl(extractImageUrl(track))
                .build();
    }

    // 내부 도우미 메서드 (아티스트 이름 추출)
    private static String extractArtists(Track track) {
        return Arrays.stream(track.getArtists())
                .map(ArtistSimplified::getName)
                .collect(Collectors.joining(", "));
    }

    // 내부 도우미 메서드 (이미지 URL 추출)
    private static String extractImageUrl(Track track) {
        if (track.getAlbum().getImages() != null && track.getAlbum().getImages().length > 0) {
            // 0: 큰거, 1: 중간, 2: 작은거. 필요에 따라 index 조정
            return track.getAlbum().getImages()[0].getUrl();
        }
        return null;
    }
}