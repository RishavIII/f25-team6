package com.f25_team6.duet.provider;

import java.util.LinkedHashSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "providers")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Provider {
    public enum SkillLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Email
    @NotBlank
    @Size(max = 254)
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    @NotBlank
    @Size(min = 50, max = 200)
    @Column(name = "password_hash", nullable = false, length = 200)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String passwordHash;

    @Size(max = 280)
    private String bio;

    @Size(max = 512)
    private String photoUrl;

    @ElementCollection
    @CollectionTable(name = "provider_instruments", joinColumns = @JoinColumn(name = "provider_id"))
    @Column(name = "instrument", length = 50, nullable = false)
    @Builder.Default
    private Set<@NotBlank @Size(max = 50) String> instruments = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "provider_genres", joinColumns = @JoinColumn(name = "provider_id"))
    @Column(name = "genre", length = 50, nullable = false)
    @Builder.Default
    private Set<@NotBlank @Size(max = 50) String> genres = new LinkedHashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 20)
    private SkillLevel skillLevel = SkillLevel.BEGINNER;

    @Builder.Default
    @Column(nullable = false)
    private boolean teachesOnline = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean teachesInPerson = false;

    @DecimalMin("0.00")
    @Digits(integer = 6, fraction = 2)
    @Column(precision = 8, scale = 2)
    private BigDecimal travelRadiusKm;

    @Size(max = 120)
    private String address;
    @Size(max = 120)
    @Size(max = 80)
    private String city;
    @Size(max = 80)
    private String state;
    @Size(max = 20)
    private String postalCode;
    @Size(max = 80)
    private String country;

    // For Map API
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;
    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 8, fraction = 2)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal hourlyRate;



}
