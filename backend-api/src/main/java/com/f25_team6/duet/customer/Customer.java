package com.f25_team6.duet.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
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

    @Builder.Default
    @Column(nullable = false)
    private boolean notifyByEmail = true;

    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 200)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String passwordHash;

    @Size(max = 20)
    private String phone;

    @Builder.Default
    @Column(nullable = false)
    private boolean notifyBySms = false;

    @Size(max = 120)
    private String addressLine1;
    @Size(max = 120)
    private String addressLine2;
    @Size(max = 80)
    private String city;
    @Size(max = 80)
    private String state;
    @Size(max = 20)
    private String postalCode;
    @Size(max = 80)
    private String country;

    // For Maps API
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Size(max = 280)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "customer_instruments", joinColumns = @JoinColumn(name = "customer_id"))
    @Column(name = "instrument", length = 50)
    @Builder.Default
    private Set<@NotBlank @Size(max = 50) String> preferredInstruments = new LinkedHashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 20)
    private SkillLevel skillLevel = SkillLevel.BEGINNER;
}
