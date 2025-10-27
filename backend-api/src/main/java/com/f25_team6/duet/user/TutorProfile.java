package com.f25_team6.duet.user;

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
}
