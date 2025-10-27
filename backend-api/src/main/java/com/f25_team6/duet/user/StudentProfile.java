package com.f25_team6.duet.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {
    @Id
    private Long userId; // FK = PK

    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text")
    private String contactPreferences;

    @Builder.Default
    @Column(nullable = false)
    private boolean prefersOnline = true;
    @Builder.Default
    @Column(nullable = false)
    private boolean prefersInPerson = false;
}
