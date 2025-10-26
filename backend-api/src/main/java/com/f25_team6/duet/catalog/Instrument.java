package com.f25_team6.duet.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name="instruments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Instrument {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @NotBlank @Size(max=100)
  @Column(nullable=false, unique=true, length=100)
  private String name;
}
