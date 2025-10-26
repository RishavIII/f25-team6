// com/f25_team6/duet/user/User.java
package com.f25_team6.duet.user;

import com.f25_team6.duet.common.BaseEntity;
import com.f25_team6.duet.common.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Email @NotBlank @Size(max=254)
  @Column(nullable=false, unique=true, length=254)
  private String email;

  @NotBlank @Size(min=60, max=100) 
  @Column(nullable=false, length=100)
  private String password;

  @NotBlank @Size(max=100)
  @Column(nullable=false, length=100)
  private String name;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable=false, length=16)
  private UserRole role = UserRole.STUDENT;

  @Size(max=40)
  private String phone;
}
