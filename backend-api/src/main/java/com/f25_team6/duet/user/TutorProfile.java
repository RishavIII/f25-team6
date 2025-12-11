package com.f25_team6.duet.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorProfile {
    @Id
    private Long userId;

    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text")
    private String bio;
    private String photoUrl;

    @Column(name = "image_data")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private byte[] photoBlob;

    @Column(name = "photo_content_type")
    private String photoContentType;

    @Builder.Default
    @Column(nullable = false)
    private boolean onlineEnabled = true;
    @Builder.Default
    @Column(nullable = false)
    private boolean inPersonEnabled = false;

    @Column(nullable = false)
    private Integer hourlyRateCents;

    private Double latitude;
    private Double longitude;
    private String city;
    private String state;
    private String timezone;

    @Builder.Default
    private Double ratingAvg = 0.0;
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(columnDefinition = "text")
    private String cancellationNote;

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<com.f25_team6.duet.catalog.TutorInstrument> instruments = new java.util.ArrayList<>();
}
